package buildcraft.lib.permission;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

import net.minecraftforge.common.UsernameCache;

import buildcraft.api.core.BCLog;
import buildcraft.api.permission.IOwner;
import buildcraft.lib.misc.WorkerThreadUtil;

public final class PlayerOwner implements IOwner {
    private static final LoadingCache<UUID, PlayerOwner> CACHE_UUID;
    private static final LoadingCache<String, PlayerOwner> CACHE_NAME;

    @Nonnull
    private static final UUID NULL_PLAYER_UUID = new UUID(0, 0);

    static {
        CACHE_UUID = CacheBuilder.newBuilder().build(CacheLoader.from(PlayerOwner::new));
        CACHE_NAME = CacheBuilder.newBuilder().build(CacheLoader.from(PlayerOwner::new));
    }

    private static PlayerOwner getOwnerFromUUID(UUID uuid) {
        return CACHE_UUID.getUnchecked(uuid);
    }

    private GameProfile owner;

    public static PlayerOwner getOwnerOf(Entity entity) {
        if (entity.worldObj.isRemote) {
            throw new IllegalArgumentException("Can only use this on the logical server!");
        }
        if (entity.getClass() == EntityPlayerMP.class) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            return CACHE_UUID.getUnchecked(player.getGameProfile().getId()).intern();
        } else {
            // TODO: Add handling for fake players + other indirect methods.
            throw new IllegalArgumentException("Unknown player entity " + entity);
        }
    }

    private PlayerOwner(UUID uuid) {
        String possibleName = UsernameCache.getLastKnownUsername(uuid);
        if (StringUtils.isNullOrEmpty(possibleName)) {
            owner = new GameProfile(uuid, null);
            fillOwner();
        } else {
            owner = new GameProfile(uuid, possibleName);
        }
    }

    private PlayerOwner(String name) {
        owner = new GameProfile(null, name);
        fillOwner();
    }

    private PlayerOwner(UUID uuid, String name) {
        owner = new GameProfile(uuid, name);
        // No need to fill in the owner if both the
        // UUID and the name are already present.
    }

    public static PlayerOwner read(PacketBuffer buffer) {
        long mostSigBits = buffer.readLong();
        long leastSigBits = buffer.readLong();
        UUID uuid = new UUID(mostSigBits, leastSigBits);
        PlayerOwner existing = getOwnerFromUUID(uuid);

        // Read the name as well, in case we are offline and it has been saved.
        String name = buffer.readStringFromBuffer(256);
        if (!StringUtils.isNullOrEmpty(name) && StringUtils.isNullOrEmpty(existing.getOwnerName())) {
            existing = new PlayerOwner(uuid, name);
        }
        return existing.intern();
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
        if (!StringUtils.isNullOrEmpty(name) && StringUtils.isNullOrEmpty(existing.getOwnerName())) {
            existing = new PlayerOwner(uuid, name);
        }
        return existing.intern();
    }

    public static PlayerOwner lookup(String name) {
        return CACHE_NAME.getUnchecked(name).intern();
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
        Future<GameProfile> future = TaskLookupGameProfile.lookupLater(owner, false);
        if (future.isDone()) {
            try {
                owner = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new Error("The task was done, but threw an error anyway! THIS IS VERY BAD!", e);
            }
        } else {
            WorkerThreadUtil.executeDependantTask(() -> {
                PlayerOwner.this.owner = future.get();
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
        return owner.getName();
    }

    private PlayerOwner intern() {
        String name = getOwnerName();
        UUID uuid = owner.getId();
        if (StringUtils.isNullOrEmpty(name)) {
            PlayerOwner existing = CACHE_UUID.getIfPresent(uuid);
            if (existing == null) {
                CACHE_UUID.put(uuid, this);
                return this;
            } else {
                return existing;
            }

        } else if (uuid == null) {
            PlayerOwner existing = CACHE_NAME.getIfPresent(name);
            if (existing == null) {
                CACHE_NAME.put(name, this);
                return this;
            } else {
                return existing;
            }
        } else {
            // Both UUID and name are here
            PlayerOwner fromUUID = CACHE_UUID.getIfPresent(uuid);
            PlayerOwner fromName = CACHE_NAME.getIfPresent(name);
            if (fromUUID == null && fromName != null) {
                CACHE_UUID.put(uuid, fromName);
                return fromName;
            } else if (fromUUID != null && fromName == null) {
                CACHE_NAME.put(name, fromUUID);
                return fromUUID;
            } else if (fromUUID == fromName) {
                return fromName;
            } else {
                // Uh oh, thats not good- somehow two separate entries exist with the same properties.
                // Don't require a debug symbol b/c I'm interested in if this actually happens in the wild
                String data = "[N=" + System.identityHashCode(fromName) + ", O=" + System.identityHashCode(fromUUID) + "]";
                BCLog.logger.warn("[lib.perm.owner] Found 2 different (but identical) PlayerOwner objects! Odd...  " + data);
                CACHE_NAME.put(name, fromUUID);
                return fromUUID;
            }
        }
    }

    @Override
    public String toString() {
        if (owner == null) {
            return "owner [ null ]";
        }
        return "owner [ " + owner.getId() + ", " + owner.getName() + " ]";
    }

    @Override
    public EntityPlayer getPlayer(MinecraftServer server) {
        PlayerList playerList = server.getPlayerList();
        if (playerList == null) {
            return null;
        }
        return playerList.getPlayerByUUID(getPlayerUUID());
    }

    @Nonnull
    @Override
    public UUID getPlayerUUID() {
        UUID id = owner.getId();
        if (id == null) {
            return NULL_PLAYER_UUID;
        }
        return id;
    }

    @Nonnull
    @Override
    public String getPlayerName() {
        String name = owner.getName();
        if (name == null) {
            return "[unknown]";
        }
        return name;
    }
}
