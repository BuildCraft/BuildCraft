package buildcraft.lib.bpt;

import java.util.Map;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import buildcraft.api.bpt.IBptTask;
import buildcraft.api.bpt.IBuilder;
import buildcraft.api.bpt.Schematic;
import buildcraft.api.bpt.Schematic.BuildStage;
import buildcraft.core.lib.utils.Utils;

public abstract class BlueprintBase {
    public BlockPos anchor;
    public BlockPos min, max;
    public EnumFacing direction;

    public BlueprintBase(BlockPos anchor, BlockPos min, BlockPos max, EnumFacing direction) {
        this.anchor = anchor;
        this.min = min;
        this.max = max;
        this.direction = direction;
    }

    protected abstract void rotateContentsBy(Rotation rotation);

    protected abstract void mirrorContents(Mirror mirror);

    /** Translates the contents of this blueprint. The anchor, min and max have been translated. */
    protected abstract void translateContentsBy(Vec3i by);

    public abstract Map<Schematic, Iterable<IBptTask>> createTasks(IBuilder builder, BuildStage stage);

    public final void translateBy(Vec3i by) {
        translateContentsBy(by);
        anchor = anchor.add(by);
        min = min.add(by);
        max = max.add(by);
    }

    public final void translateTo(BlockPos to) {
        translateBy(to.subtract(anchor));
    }

    public final void rotateBy(Rotation rotation) {
        rotateContentsBy(rotation);
        anchor = rotate(anchor, rotation);
        BlockPos minRot = rotate(min, rotation);
        BlockPos maxRot = rotate(max, rotation);
        min = Utils.min(minRot, maxRot);
        max = Utils.max(minRot, maxRot);
    }

    /** Rotates this blueprint around the origin. Will do the same as {@link #rotateAroundAnchorTo(EnumFacing)} if the
     * anchor IS the origin, or is directly above it. */
    public final void rotateTo(EnumFacing newDirection) {
        if (newDirection.getAxis() == Axis.Y) {
            throw new IllegalArgumentException("Cannot rotate to a Y-axis!");
        }
        final Rotation rotation;
        if (newDirection == direction) {
            rotation = Rotation.NONE;
        } else {
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

    public final void rotateAroundAnchorTo(EnumFacing newDirection) {
        BlockPos oldAnchor = anchor;
        translateTo(BlockPos.ORIGIN);
        rotateTo(newDirection);
        translateTo(oldAnchor);
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
