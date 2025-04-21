/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/** Class for dealing with {@link Vector3d}, {@link Vector3i}, {@link Direction}, {@link Axis} conversions and additions.
 * This is for simple functions ONLY, {@link PositionUtil} is for complex interactions */
public class VecUtil {
    public static final BlockPos POS_ONE = new BlockPos(1, 1, 1);
    public static final Vector3d VEC_HALF = new Vector3d(0.5, 0.5, 0.5);
    public static final Vector3d VEC_ONE = new Vector3d(1, 1, 1);

    public static Vector3d add(Vector3d a, Vector3i b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    public static Vector3d offset(Vector3d from, Direction direction, double by) {
        return from.add(direction.getStepX() * by, direction.getStepY() * by, direction.getStepZ() * by);
    }

    public static double dot(Vector3d a, Vector3d b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vector3d scale(Vector3d vec, double scale) {
        return vec.scale(scale);
    }

    public static Direction getFacing(Axis axis, boolean positive) {
        AxisDirection dir = positive ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        return Direction.get(dir, axis);
    }

    public static BlockPos absolute(BlockPos val) {
        return new BlockPos(Math.abs(val.getX()), Math.abs(val.getY()), Math.abs(val.getZ()));
    }

    public static Vector3d replaceValue(Vector3d old, Axis axis, double with) {
        return new Vector3d(//
                axis == Axis.X ? with : old.x,//
                axis == Axis.Y ? with : old.y,//
                axis == Axis.Z ? with : old.z//
        );
    }

    @Nonnull
    public static BlockPos replaceValue(Vector3i old, Axis axis, int with) {
        return new BlockPos(//
                axis == Axis.X ? with : old.getX(),//
                axis == Axis.Y ? with : old.getY(),//
                axis == Axis.Z ? with : old.getZ()//
        );
    }

    public static double getValue(Vector3d from, Axis axis) {
        return axis == Axis.X ? from.x : axis == Axis.Y ? from.y : from.z;
    }

    public static int getValue(Vector3i from, Axis axis) {
        return axis == Axis.X ? from.getX() : axis == Axis.Y ? from.getY() : from.getZ();
    }

    public static double getValue(Vector3d negative, Vector3d positive, Direction face) {
        switch (face) {
            case DOWN:
                return negative.y;
            case UP:
                return positive.y;
            case NORTH:
                return negative.z;
            case SOUTH:
                return positive.z;
            case WEST:
                return negative.x;
            case EAST:
                return positive.x;
            default:
                throw new IllegalArgumentException("Unknwon Direction " + face);
        }
    }

    public static int getValue(Vector3i negative, Vector3i positive, Direction face) {
        switch (face) {
            case DOWN:
                return negative.getY();
            case UP:
                return positive.getY();
            case NORTH:
                return negative.getZ();
            case SOUTH:
                return positive.getZ();
            case WEST:
                return negative.getX();
            case EAST:
                return positive.getX();
            default:
                throw new IllegalArgumentException("Unknwon Direction " + face);
        }
    }

    public static Vector3d convertCenter(Vector3i pos) {
        return new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static BlockPos convertFloor(Vector3d vec) {
        return new BlockPos(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z));
    }

    public static BlockPos convertCeiling(Vector3d vec) {
        return new BlockPos(Math.ceil(vec.x), Math.ceil(vec.y), Math.ceil(vec.z));
    }

    public static Tuple3f convertFloat(Vector3d vec) {
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }

    // Min/Max

    public static BlockPos min(BlockPos a, BlockPos b) {
        if (a == null) return b;
        if (b == null) return a;
        return new BlockPos(//
                Math.min(a.getX(), b.getX()),//
                Math.min(a.getY(), b.getY()),//
                Math.min(a.getZ(), b.getZ())//
        );
    }

    public static BlockPos min(BlockPos a, BlockPos b, BlockPos c) {
        return min(min(a, b), c);
    }

    public static BlockPos min(BlockPos a, BlockPos b, BlockPos c, BlockPos d) {
        return min(min(a, b), min(c, d));
    }

    public static BlockPos max(BlockPos a, BlockPos b) {
        if (a == null) return b;
        if (b == null) return a;
        return new BlockPos(//
                Math.max(a.getX(), b.getX()),//
                Math.max(a.getY(), b.getY()),//
                Math.max(a.getZ(), b.getZ())//
        );
    }

    public static BlockPos max(BlockPos a, BlockPos b, BlockPos c) {
        return max(max(a, b), c);
    }

    public static BlockPos max(BlockPos a, BlockPos b, BlockPos c, BlockPos d) {
        return max(max(a, b), max(c, d));
    }

    public static Vector3d min(Vector3d a, Vector3d b) {
        if (a == null) return b;
        if (b == null) return a;
        return new Vector3d(//
                Math.min(a.x, b.x),//
                Math.min(a.y, b.y),//
                Math.min(a.z, b.z)//
        );
    }

    public static Vector3d min(Vector3d a, Vector3d b, Vector3d c) {
        return min(min(a, b), c);
    }

    public static Vector3d min(Vector3d a, Vector3d b, Vector3d c, Vector3d d) {
        return min(min(a, b), min(c, d));
    }

    public static Vector3d max(Vector3d a, Vector3d b) {
        if (a == null) return b;
        if (b == null) return a;
        return new Vector3d(//
                Math.max(a.x, b.x),//
                Math.max(a.y, b.y),//
                Math.max(a.z, b.z)//
        );
    }

    public static Vector3d max(Vector3d a, Vector3d b, Vector3d c) {
        return max(max(a, b), c);
    }

    public static Vector3d max(Vector3d a, Vector3d b, Vector3d c, Vector3d d) {
        return max(max(a, b), max(c, d));
    }

    /** {@link Vec3i#distanceSq(Vec3i)} in 1.12.2 and {@link Vec3i#distSqr(Vec3i)} in 1.18.2 returns the distance to the lower connor,
     * but {@link Vector3i#distSqr(Vector3i)} in 1.16.5 returns the distance from the lower conner to the block center.
     * What is bugjump doing??? */
    public static double distanceSq(Vector3i pos1, Vector3i pos2) {
        double d1 = ((double) pos1.getX()) - (double) pos2.getX();
        double d2 = ((double) pos1.getY()) - (double) pos2.getY();
        double d3 = ((double) pos1.getZ()) - (double) pos2.getZ();
        return d1 * d1 + d2 * d2 + d3 * d3;
    }

    // Calen 1.18.2: from 1.8.9

    /** Factory that converts an integer vector to a double vector. */
    public static Vector3d convert(Vector3i vec3i) {
        return new Vector3d(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static Vector3d convert(Direction face) {
        if (face == null) {
            return Vector3d.ZERO;
        }
        return new Vector3d(face.getStepX(), face.getStepY(), face.getStepZ());
    }

    public static Vector3d convert(Direction face, double size) {
        return scale(convert(face), size);
    }

    public static Vector3d getVec(Entity entity) {
        return new Vector3d(entity.getX(), entity.getY(), entity.getZ());
    }

    public static BlockPos getPos(Entity entity) {
        return convertFloor(entity.position());
    }
}
