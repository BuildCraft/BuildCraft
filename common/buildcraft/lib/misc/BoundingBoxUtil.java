package buildcraft.lib.misc;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.IBox;

/** Various methods operating on (and creating) {@link AxisAlignedBB} */
public class BoundingBoxUtil {

    /** Creates an {@link AxisAlignedBB} from a block pos and a box.
     * 
     * Note that additional must NOT be null, but the box can be. */
    public static AxisAlignedBB makeFrom(BlockPos additional, IBox box) {
        if (box == null) {
            return new AxisAlignedBB(additional);
        } else {
            BlockPos min = VecUtil.min(box.min(), additional);
            BlockPos max = VecUtil.max(box.max(), additional);
            return makeFrom(min, max);
        }
    }

    /** Creates an {@link AxisAlignedBB} from a block pos and 2 boxes
     * 
     * Note that additional must NOT be null, but (either of) boxes can be. */
    public static AxisAlignedBB makeFrom(BlockPos additional, IBox box1, IBox box2) {
        if (box1 == null) {
            return makeFrom(additional, box2);
        } else if (box2 == null) {
            return makeFrom(additional, box1);
        } else {
            BlockPos min = VecUtil.min(box1.min(), box2.min(), additional);
            BlockPos max = VecUtil.max(box1.max(), box2.max(), additional);
            return makeFrom(min, max);
        }
    }

    /** Creates an {@link AxisAlignedBB} that fully encompasses the two given block min and max. */
    public static AxisAlignedBB makeFrom(BlockPos min, BlockPos max) {
        return new AxisAlignedBB(min, max.add(VecUtil.POS_ONE));
    }
}
