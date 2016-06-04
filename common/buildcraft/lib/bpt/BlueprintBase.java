package buildcraft.lib.bpt;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.VecUtil;

public abstract class BlueprintBase {
    public BlockPos size;
    public EnumFacing direction;

    public BlueprintBase(BlockPos size, EnumFacing direction) {
        this.size = size;
        this.direction = direction;
    }

    public BlueprintBase(NBTTagCompound nbt) {
        this.size = NBTUtils.readBlockPos(nbt.getTag("size"));
        this.direction = NBTUtils.readEnum(nbt.getTag("direction"), EnumFacing.class);
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("size", NBTUtils.writeBlockPos(size));
        nbt.setTag("direction", NBTUtils.writeEnum(direction));
        return nbt;
    }

    protected abstract void rotateContentsBy(Rotation rotation);

    public abstract List<Iterable<IBptTask>> createTasks(IBuilder builder, BlockPos from);

    public final void rotateBy(Rotation rotation) {
        rotateContentsBy(rotation);
        size = rotate(size, rotation);
        size = VecUtil.absolute(size);
    }

    /** Rotates this blueprint around the origin. */
    public final void rotateTo(EnumFacing newDirection) {
        if (newDirection.getAxis() == Axis.Y) {
            throw new IllegalArgumentException("Cannot rotate to a Y-axis!");
        }
        final Rotation rotation;
        if (newDirection == direction) {
            rotation = Rotation.NONE;
        } else {
            // This is icky. Just use a switch or maths or something like that.
            before_assign: do {
                for (Rotation rot : Rotation.values()) {
                    if (newDirection == rot.rotate(direction)) {
                        rotation = rot;
                        break before_assign;
                    }
                }
                throw new IllegalStateException("Invalid starting direction " + direction);
            } while (false);
        }
        if (rotation != Rotation.NONE) rotateBy(rotation);
    }

    /** Rotates the given point around the origin by the given rotation. */
    protected final static BlockPos rotate(BlockPos from, Rotation rotation) {
        BlockPos normalized = from;
        BlockPos rotated = new BlockPos(0, normalized.getY(), 0);
        int numEast = normalized.getX();
        int numSouth = normalized.getZ();
        EnumFacing newEast = rotation.rotate(EnumFacing.EAST);
        EnumFacing newSouth = rotation.rotate(EnumFacing.SOUTH);
        rotated = Utils.withValue(rotated, newEast.getAxis(), numEast * newEast.getAxisDirection().getOffset());
        rotated = Utils.withValue(rotated, newSouth.getAxis(), numSouth * newSouth.getAxisDirection().getOffset());
        return rotated;
    }
}
