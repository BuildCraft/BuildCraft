/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.IBox;
import buildcraft.lib.misc.data.Box;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Collection;

/** Various methods operating on (and creating) {@link AxisAlignedBB} */
public class BoundingBoxUtil {

    /** Creates an {@link AxisAlignedBB} from a block pos and a box. Note that additional must NOT be null, but the box
     * can be. */
    public static AxisAlignedBB makeFrom(BlockPos additional, @Nullable IBox box) {
        if (box == null) {
            return new AxisAlignedBB(additional);
        } else {
            BlockPos min = VecUtil.min(box.min(), additional);
            BlockPos max = VecUtil.max(box.max(), additional);
            return new AxisAlignedBB(min, max.offset(VecUtil.POS_ONE));
        }
    }

    public static AxisAlignedBB makeFrom(BlockPos primary, BlockPos... additional) {
        Box box = new Box(primary, primary);
        for (BlockPos a : additional) {
            box.extendToEncompass(a);
        }
        return box.getBoundingBox();
    }

    /** Creates an {@link AxisAlignedBB} from a block     pos and 2 boxes Note that additional must NOT be null, but (either
     * of) the boxes can be. */
    public static AxisAlignedBB makeFrom(BlockPos additional, @Nullable IBox box1, @Nullable IBox box2) {
        if (box1 == null) {
            return makeFrom(additional, box2);
        } else if (box2 == null) {
            return makeFrom(additional, box1);
        } else {
            BlockPos min = VecUtil.min(box1.min(), box2.min(), additional);
            BlockPos max = VecUtil.max(box1.max(), box2.max(), additional);
            return new AxisAlignedBB(min, max.offset(VecUtil.POS_ONE));
        }
    }

    public static AxisAlignedBB makeFrom(Vector3d from, Vector3d to) {
        return new AxisAlignedBB(from.x, from.y, from.z, to.x, to.y, to.z);
    }

    public static VoxelShape makeVoxelShapeFrom(Vector3d from, Vector3d to) {
        return VoxelShapes.box(from.x, from.y, from.z, to.x, to.y, to.z);
    }

    public static VoxelShape makeVoxelShapeFrom(Vector3d from, Vector3d to, double radius) {
        return VoxelShapes.create(makeFrom(from, to, radius));
    }

    public static AxisAlignedBB makeFrom(Vector3d from, Vector3d to, double radius) {
        return makeFrom(from, to).inflate(radius);
    }

    public static AxisAlignedBB makeAround(Vector3d around, double radius) {
        return new AxisAlignedBB(around.x, around.y, around.z, around.x, around.y, around.z).inflate(radius);
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
        return new AxisAlignedBB(min, max.offset(VecUtil.POS_ONE));
    }

    /** Creates a box that extrudes from the specified face of the given block position. */
    public static AxisAlignedBB extrudeFace(BlockPos pos, Direction face, double depth) {
        Vector3d from = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
        Vector3d to = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(1, 1, 1);

        Axis axis = face.getAxis();
        if (face.getAxisDirection() == AxisDirection.POSITIVE) {
            from = VecUtil.replaceValue(from, axis, VecUtil.getValue(from, axis) + 1);
            to = VecUtil.replaceValue(to, axis, VecUtil.getValue(to, axis) + depth);
        } else {
            to = VecUtil.replaceValue(to, axis, VecUtil.getValue(to, axis) - 1);
            from = VecUtil.replaceValue(from, axis, VecUtil.getValue(from, axis) - depth);
        }
        return makeFrom(from, to);
    }
}
