/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.misc;

import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {
    // Use FQN to avoid clashing with buildcraft.lib.misc.data.Box
    public static net.minecraft.util.math.Box rotateAABB(net.minecraft.util.math.Box aabb, Direction facing) {
        if (facing == Direction.DOWN) {
            return new net.minecraft.util.math.Box(aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
        } else if (facing == Direction.UP) {
            return new net.minecraft.util.math.Box(aabb.minX, 1 - aabb.maxY, aabb.minZ, aabb.maxX, 1 - aabb.minY, aabb.maxZ);
        } else if (facing == Direction.NORTH) {
            return new net.minecraft.util.math.Box(aabb.minX, aabb.minZ, aabb.minY, aabb.maxX, aabb.maxZ, aabb.maxY);
        } else if (facing == Direction.SOUTH) {
            return new net.minecraft.util.math.Box(aabb.minX, aabb.minZ, 1 - aabb.maxY, aabb.maxX, aabb.maxZ, 1 - aabb.minY);
        } else if (facing == Direction.WEST) {
            return new net.minecraft.util.math.Box(aabb.minY, aabb.minZ, aabb.minX, aabb.maxY, aabb.maxZ, aabb.maxX);
        } else if (facing == Direction.EAST) {
            return new net.minecraft.util.math.Box(1 - aabb.maxY, aabb.minZ, aabb.minX, 1 - aabb.minY, aabb.maxZ, aabb.maxX);
        }
        return aabb;
    }

    public static Vec3d rotateVec3d(Vec3d vec, BlockRotation rotation) {
        switch (rotation) {
            case NONE:
            default:
                return vec;
            case CLOCKWISE_90:
                return new Vec3d(1 - vec.z, vec.y, vec.x);
            case CLOCKWISE_180:
                return new Vec3d(1 - vec.x, vec.y, 1 - vec.z);
            case COUNTERCLOCKWISE_90:
                return new Vec3d(vec.z, vec.y, 1 - vec.x);
        }
    }

    public static Direction rotateAll(Direction facing) {
        switch (facing) {
            case NORTH:
                return Direction.EAST;
            case EAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.WEST;
            case WEST:
                return Direction.UP;
            case UP:
                return Direction.DOWN;
            case DOWN:
                return Direction.NORTH;
        }
        throw new IllegalArgumentException();
    }

    public static BlockRotation invert(BlockRotation rotation) {
        switch (rotation) {
            case NONE:
                return BlockRotation.NONE;
            case CLOCKWISE_90:
                return BlockRotation.COUNTERCLOCKWISE_90;
            case CLOCKWISE_180:
                return BlockRotation.CLOCKWISE_180;
            case COUNTERCLOCKWISE_90:
                return BlockRotation.CLOCKWISE_90;
        }
        throw new IllegalArgumentException();
    }
}
