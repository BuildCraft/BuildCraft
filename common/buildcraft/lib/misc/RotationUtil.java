/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {
    public static AxisAlignedBB rotateAABB(AxisAlignedBB aabb, EnumFacing facing) {
        if (facing == EnumFacing.DOWN) {
            return new AxisAlignedBB(aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
        } else if (facing == EnumFacing.UP) {
            return new AxisAlignedBB(aabb.minX, 1 - aabb.maxY, aabb.minZ, aabb.maxX, 1 - aabb.minY, aabb.maxZ);
        } else if (facing == EnumFacing.NORTH) {
            return new AxisAlignedBB(aabb.minX, aabb.minZ, aabb.minY, aabb.maxX, aabb.maxZ, aabb.maxY);
        } else if (facing == EnumFacing.SOUTH) {
            return new AxisAlignedBB(aabb.minX, aabb.minZ, 1 - aabb.maxY, aabb.maxX, aabb.maxZ, 1 - aabb.minY);
        } else if (facing == EnumFacing.WEST) {
            return new AxisAlignedBB(aabb.minY, aabb.minZ, aabb.minX, aabb.maxY, aabb.maxZ, aabb.maxX);
        } else if (facing == EnumFacing.EAST) {
            return new AxisAlignedBB(1 - aabb.maxY, aabb.minZ, aabb.minX, 1 - aabb.minY, aabb.maxZ, aabb.maxX);
        }
        return aabb;
    }

    public static Vec3d rotateVec3d(Vec3d vec, Rotation rotation) {
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

    public static EnumFacing rotateAll(EnumFacing facing) {
        switch (facing) {
            case NORTH:
                return EnumFacing.EAST;
            case EAST:
                return EnumFacing.SOUTH;
            case SOUTH:
                return EnumFacing.WEST;
            case WEST:
                return EnumFacing.UP;
            case UP:
                return EnumFacing.DOWN;
            case DOWN:
                return EnumFacing.NORTH;
        }
        throw new IllegalArgumentException();
    }
}
