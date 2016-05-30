package buildcraft.lib.permission;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import buildcraft.lib.misc.WorkerThreadUtil;

public final class PlayerOwner {
    private GameProfile owner;

    public PlayerOwner(EntityPlayer player) {
        if (player.getClass() != EntityPlayerMP.class) {
            throw new IllegalArgumentException("Invalid player class " + player.getClass());
        }
        this.owner = player.getGameProfile();
        fillOwner();
    }

    public PlayerOwner(PacketBuffer buffer) {
        long mostSigBits = buffer.readLong();
        long leastSigBits = buffer.readLong();
        UUID uuid = new UUID(mostSigBits, leastSigBits);
        owner = new GameProfile(uuid, null);
        fillOwner();
    }

    public PlayerOwner(NBTTagCompound nbt) {
        long mostSigBits = nbt.getLong("most");
        long leastSigBits = nbt.getLong("least");
        UUID uuid = new UUID(mostSigBits, leastSigBits);
        owner = new GameProfile(uuid, null);
        fillOwner();
    }

    public void writeToByteBuff(PacketBuffer buffer) {
        buffer.writeLong(owner.getId().getMostSignificantBits());
        buffer.writeLong(owner.getId().getLeastSignificantBits());
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("most", owner.getId().getMostSignificantBits());
        nbt.setLong("least", owner.getId().getLeastSignificantBits());
        return nbt;
    }

    public void fillOwner() {
        Future<GameProfile> future = TaskLookupGameProfile.lookupLater(owner);
        if (future.isDone()) {
            try {
                owner = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new Error("The task was done, but threw an error anyway! THIS IS VERY BAD!", e);
            }
        } else {
            WorkerThreadUtil.executeWorkTask(() -> {
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
}
