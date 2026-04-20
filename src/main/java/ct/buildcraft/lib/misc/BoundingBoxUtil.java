/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.util.Collection;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.IBox;
import ct.buildcraft.lib.misc.data.Box;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Various methods operating on (and creating) {@link AABB} */
public class BoundingBoxUtil {

    /** Creates an {@link AABB} from a block pos and a box. Note that additional must NOT be null, but the box
     * can be. */
    public static AABB makeFrom(BlockPos additional, @Nullable IBox box) {
        if (box == null) {
            return new AABB(additional);
        } else {
            BlockPos min = VecUtil.min(box.min(), additional);
            BlockPos max = VecUtil.max(box.max(), additional);
            return new AABB(min, max.offset(VecUtil.POS_ONE));
        }
    }

    public static AABB makeFrom(BlockPos primary, BlockPos... additional) {
        Box box = new Box(primary, primary);
        for (BlockPos a : additional) {
            box.extendToEncompass(a);
        }
        return box.getBoundingBox();
    }

    /** Creates an {@link AABB} from a block pos and 2 boxes Note that additional must NOT be null, but (either
     * of) the boxes can be. */
    public static AABB makeFrom(BlockPos additional, @Nullable IBox box1, @Nullable IBox box2) {
        if (box1 == null) {
            return makeFrom(additional, box2);
        } else if (box2 == null) {
            return makeFrom(additional, box1);
        } else {
            BlockPos min = VecUtil.min(box1.min(), box2.min(), additional);
            BlockPos max = VecUtil.max(box1.max(), box2.max(), additional);
            return new AABB(min, max.offset(VecUtil.POS_ONE));
        }
    }

    public static AABB makeFrom(Vec3 from, Vec3 to) {
        return new AABB(from.x, from.y, from.z, to.x, to.y, to.z);
    }

    public static AABB makeFrom(Vec3 from, Vec3 to, double radius) {
        return makeFrom(from, to).inflate(radius);
    }
    
    public static AABB makeAround(Vec3 around, double radius) {
        return new AABB(around.x, around.y, around.z, around.x, around.y, around.z).inflate(radius);
    }

    public static AABB makeFrom(BlockPos pos, @Nullable IBox box, @Nullable Collection<BlockPos> additional) {
        BlockPos min = box == null ? pos : VecUtil.min(box.min(), pos);
        BlockPos max = box == null ? pos : VecUtil.max(box.max(), pos);
        if (additional != null) {
            for (BlockPos p : additional) {
                min = VecUtil.min(min, p);
                max = VecUtil.max(max, p);
            }
        }
        return new AABB(min, max.offset(VecUtil.POS_ONE));
    }

    /** Creates a box that extrudes from the specified face of the given block position. */
    public static AABB extrudeFace(BlockPos pos, Direction face, double depth) {
        Vec3 from = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        Vec3 to = new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(1, 1, 1);

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
