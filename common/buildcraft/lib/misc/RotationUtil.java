/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.transport.EnumWirePart;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class RotationUtil {
    public static AxisAlignedBB rotateAABB(AxisAlignedBB aabb, Direction facing) {
        if (facing == Direction.DOWN) {
            return new AxisAlignedBB(aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
        } else if (facing == Direction.UP) {
            return new AxisAlignedBB(aabb.minX, 1 - aabb.maxY, aabb.minZ, aabb.maxX, 1 - aabb.minY, aabb.maxZ);
        } else if (facing == Direction.NORTH) {
            return new AxisAlignedBB(aabb.minX, aabb.minZ, aabb.minY, aabb.maxX, aabb.maxZ, aabb.maxY);
        } else if (facing == Direction.SOUTH) {
            return new AxisAlignedBB(aabb.minX, aabb.minZ, 1 - aabb.maxY, aabb.maxX, aabb.maxZ, 1 - aabb.minY);
        } else if (facing == Direction.WEST) {
            return new AxisAlignedBB(aabb.minY, aabb.minZ, aabb.minX, aabb.maxY, aabb.maxZ, aabb.maxX);
        } else if (facing == Direction.EAST) {
            return new AxisAlignedBB(1 - aabb.maxY, aabb.minZ, aabb.minX, 1 - aabb.minY, aabb.maxZ, aabb.maxX);
        }
        return aabb;
    }

    public static Vector3d rotateVec3d(Vector3d vec, Rotation rotation) {
        switch (rotation) {
            case NONE:
            default:
                return vec;
            case CLOCKWISE_90:
                return new Vector3d(1 - vec.z, vec.y, vec.x);
            case CLOCKWISE_180:
                return new Vector3d(1 - vec.x, vec.y, 1 - vec.z);
            case COUNTERCLOCKWISE_90:
                return new Vector3d(vec.z, vec.y, 1 - vec.x);
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

    public static Rotation invert(Rotation rotation) {
        switch (rotation) {
            case NONE:
                return Rotation.NONE;
            case CLOCKWISE_90:
                return Rotation.COUNTERCLOCKWISE_90;
            case CLOCKWISE_180:
                return Rotation.CLOCKWISE_180;
            case COUNTERCLOCKWISE_90:
                return Rotation.CLOCKWISE_90;
        }
        throw new IllegalArgumentException();
    }

    /** This method is defined in 1.12.2 as rotateAround and in 1.18.2 as getClockWise,
     * but absent in 1.16.5. */
    public static Direction rotateAround(Direction from, Axis axis) {
        switch (axis) {
            case X:
                if (from != Direction.WEST && from != Direction.EAST) {
                    return rotateX(from);
                }
                return from;
            case Y:
                if (from != Direction.UP && from != Direction.DOWN) {
                    return rotateY(from);
                }
                return from;
            case Z:
                if (from != Direction.NORTH && from != Direction.SOUTH) {
                    return rotateZ(from);
                }
                return from;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    private static Direction rotateX(Direction from) {
        Direction direction;
        switch (from) {
            case DOWN:
                direction = Direction.SOUTH;
                break;
            case UP:
                direction = Direction.NORTH;
                break;
            case NORTH:
                direction = Direction.DOWN;
                break;
            case SOUTH:
                direction = Direction.UP;
                break;
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + from);
        }

        return direction;
    }

    public static Direction rotateY(Direction from) {
        Direction direction;
        switch (from) {
            case NORTH:
                direction = Direction.EAST;
                break;
            case SOUTH:
                direction = Direction.WEST;
                break;
            case WEST:
                direction = Direction.NORTH;
                break;
            case EAST:
                direction = Direction.SOUTH;
                break;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + from);
        }

        return direction;
    }

    private static Direction rotateZ(Direction from) {
        Direction direction;
        switch (from) {
            case DOWN:
                direction = Direction.WEST;
                break;
            case UP:
                direction = Direction.EAST;
                break;
            case NORTH:
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + from);
            case WEST:
                direction = Direction.UP;
                break;
            case EAST:
                direction = Direction.DOWN;
        }

        return direction;
    }

    // Calen
    public static Direction rotateFacing(Direction facing, Rotation rotation) {
        return rotation.rotate(facing);
    }

    public static byte rotateFacing(byte facingIndex, Rotation rotation) {
        Direction facing = Direction.from3DDataValue(facingIndex);
        return (byte) rotateFacing(facing, rotation).get3DDataValue();
    }

    public static EnumWirePart rotateEnumWirePart(EnumWirePart old, Rotation rotation) {
        String thisName = old.name();
        String[] thisNamePieces = thisName.split("_");
        Direction facing0 = Direction.valueOf(thisNamePieces[0]);
        Direction facing1 = Direction.valueOf(thisNamePieces[1]);
        Direction facing2 = Direction.valueOf(thisNamePieces[2]);
        Direction facing0New = RotationUtil.rotateFacing(facing0, rotation);
        Direction facing1New = facing1;
        Direction facing2New = RotationUtil.rotateFacing(facing2, rotation);
        String newName;
        if (facing0New.getAxis() == Axis.X) {
            newName = facing0New.name() + "_" + facing1New.name() + "_" + facing2New.name();
        } else {
            newName = facing2New.name() + "_" + facing1New.name() + "_" + facing0New.name();
        }
        return EnumWirePart.valueOf(newName);
    }
}
