/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Collection;
import javax.annotation.Nullable;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.IBox;

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
        return new AxisAlignedBB(from.x, from.y, from.z, to.x, to.y, to.z);
    }

    public static AxisAlignedBB makeFrom(Vec3d from, Vec3d to, double radius) {
        return makeFrom(from, to).expand(radius, radius, radius);
    }

    public static AxisAlignedBB makeAround(Vec3d around, double radius) {
        return new AxisAlignedBB(around.subtract(radius, radius, radius), around.addVector(radius, radius, radius));
    }

    public static AxisAlignedBB makeFrom(BlockPos pos, @Nullable IBox box, @Nullable Collection<BlockPos> additional) {
        BlockPos min = box == null ? pos : VecUtil.min(box.min(), pos);
        BlockPos max = box == null ? pos : VecUtil.max(box.max(), pos);
        if (additional != null) {
            for (BlockPos p : additional) {
                min = VecUtil.min(min, p);
                max = VecUtil.max(max, p);
            }
        }
        return new AxisAlignedBB(min, max.add(VecUtil.POS_ONE));
    }
}
