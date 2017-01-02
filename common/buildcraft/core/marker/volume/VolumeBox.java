package buildcraft.core.marker.volume;

import buildcraft.lib.misc.data.Box;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.UUID;

public class VolumeBox {
    public Box box;
    private String player = null;
    private String oldPlayer = null;
    private BlockPos held = null;
    private double dist = 0;
    private BlockPos oldMin = null, oldMax = null;

    public VolumeBox(BlockPos at) {
        box = new Box(at, at);
    }

    public VolumeBox(NBTTagCompound nbt) {
        box = new Box();
        box.initialize(nbt.getCompoundTag("box"));
        player = nbt.hasKey("player") ? nbt.getString("player") : null;
        oldPlayer = nbt.hasKey("oldPlayer") ? nbt.getString("oldPlayer") : null;
        if (nbt.hasKey("held")) {
            held = NBTUtil.getPosFromTag(nbt.getCompoundTag("held"));
        }
        dist = nbt.getDouble("dist");
        if (nbt.hasKey("oldMin")) {
            oldMin = NBTUtil.getPosFromTag(nbt.getCompoundTag("oldMin"));
        }
        if (nbt.hasKey("oldMax")) {
            oldMax = NBTUtil.getPosFromTag(nbt.getCompoundTag("oldMax"));
        }
    }

    public VolumeBox(PacketBuffer buf) {
        box = new Box();
        box.readData(buf);
        player = buf.readBoolean() ? buf.readString(1024) : null;
    }

    public boolean isEditing() {
        return player != null;
    }

    private void resetEditing() {
        oldMin = oldMax = null;
        held = null;
        dist = 0;
    }

    public void cancelEditing() {
        player = null;
        box.reset();
        box.extendToEncompass(oldMin);
        box.extendToEncompass(oldMax);
        resetEditing();
    }

    public void confirmEditing() {
        player = null;
        resetEditing();
    }

    public void pauseEditing() {
        oldPlayer = player;
        player = null;
    }

    public void resumeEditing() {
        player = oldPlayer;
        oldPlayer = null;
    }

    public void setPlayer(EntityPlayer player) {
        this.player = player.getGameProfile().getId().toString();
    }

    public boolean isEditingBy(EntityPlayer player) {
        return this.player != null && Objects.equals(this.player, player.getGameProfile().getId().toString());
    }

    public boolean isPausedEditingBy(EntityPlayer player) {
        return this.oldPlayer != null && Objects.equals(this.oldPlayer, player.getGameProfile().getId().toString());
    }

    public EntityPlayer getPlayer(World world) {
        return world.getPlayerEntityByUUID(UUID.fromString(player));
    }

    public void setHeldDistOldMinOldMax(BlockPos held, double dist, BlockPos oldMin, BlockPos oldMax) {
        this.held = held;
        this.dist = dist;
        this.oldMin = oldMin;
        this.oldMax = oldMax;
    }

    public BlockPos getHeld() {
        return held;
    }

    public double getDist() {
        return dist;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("box", this.box.writeToNBT());
        if (player != null) {
            nbt.setString("player", player);
        }
        if (oldPlayer != null) {
            nbt.setString("oldPlayer", oldPlayer);
        }
        if (held != null) {
            nbt.setTag("held", NBTUtil.createPosTag(held));
        }
        nbt.setDouble("dist", dist);
        if (oldMin != null) {
            nbt.setTag("oldMin", NBTUtil.createPosTag(oldMin));
        }
        if (oldMax != null) {
            nbt.setTag("oldMax", NBTUtil.createPosTag(oldMax));
        }
        return nbt;
    }

    public void toBytes(PacketBuffer buf) {
        this.box.writeData(buf);
        buf.writeBoolean(player != null);
        if (player != null) {
            buf.writeString(player);
        }
    }
}
