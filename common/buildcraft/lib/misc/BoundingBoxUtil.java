package buildcraft.lib.misc;

import javax.annotation.Nullable;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.IBox;
import net.minecraft.util.math.Vec3d;

/** Various methods operating on (and creating) {@link AxisAlignedBB} */
public class BoundingBoxUtil {

    /** Creates an {@link AxisAlignedBB} from a block pos and a box.
     * 
     * Note that additional must NOT be null, but the box can be. */
    public static AxisAlignedBB makeFrom(BlockPos additional, @Nullable IBox box) {
        if (box == null) {
            return new AxisAlignedBB(additional);
        } else {
            BlockPos min = VecUtil.min(box.min(), additional);
            BlockPos max = VecUtil.max(box.max(), additional);
            return new AxisAlignedBB(min, max.add(VecUtil.POS_ONE));
        }
    }

    /** Creates an {@link AxisAlignedBB} from a block pos and 2 boxes
     * 
     * Note that additional must NOT be null, but (either of) the boxes can be. */
    public static AxisAlignedBB makeFrom(BlockPos additional, @Nullable IBox box1, @Nullable IBox box2) {
        if (box1 == null) {
            return makeFrom(additional, box2);
        } else if (box2 == null) {
            return makeFrom(additional, box1);
        } else {
            BlockPos min = VecUtil.min(box1.min(), box2.min(), additional);
            BlockPos max = VecUtil.max(box1.max(), box2.max(), additional);
            return new AxisAlignedBB(min, max.add(VecUtil.POS_ONE));
        }
    }

    public static AxisAlignedBB makeFrom(Vec3d from, Vec3d to) {
        return new AxisAlignedBB(
                Math.min(from.xCoord, to.xCoord),
                Math.min(from.yCoord, to.yCoord),
                Math.min(from.zCoord, to.zCoord),
                Math.max(from.xCoord, to.xCoord),
                Math.max(from.yCoord, to.yCoord),
                Math.max(from.zCoord, to.zCoord)
        );
    }

    public static AxisAlignedBB makeFrom(Vec3d from, Vec3d to, double radius) {
        return makeFrom(from, to).expandXyz(radius);
    }

    public static AxisAlignedBB makeAround(Vec3d around, double radius) {
        return new AxisAlignedBB(around.subtract(radius, radius, radius), around.addVector(radius, radius, radius));
    }
}
