package buildcraft.lib.permission;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

import buildcraft.lib.misc.WorkerThreadUtil;

public final class PlayerOwner {
    private static final String NAME_UNKNOWN = "Unknown";
    private static final LoadingCache<UUID, PlayerOwner> CACHE;

    static {
        CACHE = CacheBuilder.newBuilder().build(CacheLoader.from(PlayerOwner::new));
    }

    private static PlayerOwner getOwnerFromUUID(UUID uuid) {
        return CACHE.getUnchecked(uuid);
    }

    private GameProfile owner;
    private String potentialName = "Loading...";

    public static PlayerOwner getOwnerOf(Entity entity) {
        if (entity.worldObj.isRemote) {
            throw new IllegalArgumentException("Can only use this on the logical server!");
        }
        if (entity.getClass() == EntityPlayerMP.class) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            return CACHE.getUnchecked(player.getGameProfile().getId());
        } else {
            // TODO: Add handling for fake players + other indirect methods.
            throw new IllegalArgumentException("Unknown player entity " + entity);
        }
    }

    private PlayerOwner(UUID uuid) {
        owner = new GameProfile(uuid, null);
        fillOwner();
    }

    private PlayerOwner(String name) {
        owner = new GameProfile(null, name);
        fillOwner();
    }

    private PlayerOwner(UUID uuid, String name) {
        owner = new GameProfile(uuid, name);
        fillOwner();
    }

    public static PlayerOwner read(PacketBuffer buffer) {
        long mostSigBits = buffer.readLong();
        long leastSigBits = buffer.readLong();
        UUID uuid = new UUID(mostSigBits, leastSigBits);
        PlayerOwner existing = getOwnerFromUUID(uuid);

        // Read the name as well, in case we are offline and it has been saved.
        String name = buffer.readStringFromBuffer(256);
        if (!StringUtils.isNullOrEmpty(name)) {
            existing.potentialName = name;
        }
        return existing;
    }

    public void writeToByteBuf(PacketBuffer buffer) {
        buffer.writeLong(owner.getId().getMostSignificantBits());
        buffer.writeLong(owner.getId().getLeastSignificantBits());
        String name = owner.getName();
        if (StringUtils.isNullOrEmpty(name)) {
            buffer.writeString("");
        } else {
            buffer.writeString(name.substring(0, Math.min(name.length(), 255)));
        }
    }

    public static PlayerOwner read(NBTTagCompound nbt) {
        long mostSigBits = nbt.getLong("most");
        long leastSigBits = nbt.getLong("least");
        UUID uuid = new UUID(mostSigBits, leastSigBits);
        PlayerOwner existing = getOwnerFromUUID(uuid);

        // Read the name as well, in case we are offline and it has been saved.
        String name = nbt.getString("name");
        if (!StringUtils.isNullOrEmpty(name)) {
            existing.potentialName = name;
        }
        return existing;
    }

    public static PlayerOwner lookup(String name) {
        for (PlayerOwner existing : CACHE.asMap().values()) {
            if (!existing.owner.isComplete()) {
                continue;
            }
            if (StringUtils.isNullOrEmpty(existing.getOwnerName())) {
                continue;
            }
            if (existing.getOwnerName().equals(name)) {
                return existing;
            }
        }
        return new PlayerOwner(name);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (owner.getId() != null) {
            nbt.setLong("most", owner.getId().getMostSignificantBits());
            nbt.setLong("least", owner.getId().getLeastSignificantBits());
        }
        String name = owner.getName();
        if (!StringUtils.isNullOrEmpty(name)) {
            nbt.setString("name", name);
        } else if (owner.getId() == null) {
            throw new IllegalStateException("Null ID and name!");
        }
        return nbt;
    }

    public void fillOwner() {
        Future<GameProfile> future = TaskLookupGameProfile.lookupLater(owner);
        if (future.isDone()) {
            try {
                owner = future.get();
                potentialName = NAME_UNKNOWN;
            } catch (InterruptedException | ExecutionException e) {
                throw new Error("The task was done, but threw an error anyway! THIS IS VERY BAD!", e);
            }
        } else {
            WorkerThreadUtil.executeWorkTask(() -> {
                PlayerOwner.this.owner = future.get();
                PlayerOwner.this.potentialName = NAME_UNKNOWN;
                return null;
            });
        }
    }

    public GameProfile getOwner() {
        return this.owner;
    }

    public EntityPlayer getOwnerAsPlayer(World world) {
        return world.getPlayerEntityByUUID(getOwner().getId());
    }

    public String getOwnerName() {
        String name = owner.getName();
        if (StringUtils.isNullOrEmpty(name)) {
            return potentialName;
        }
        return name;
    }

    /** Returns the internalised version of this. Only happens if this has been completed. */
    public PlayerOwner intern() {
        if (!owner.isComplete()) {
            return this;
        }
        UUID uuid = owner.getId();
        PlayerOwner existing = CACHE.getIfPresent(uuid);
        if (existing == null) {
            CACHE.put(uuid, this);
            return this;
        } else if (existing.owner.isComplete()) {
            return existing;
        } else {
            return this;
        }
    }
}
