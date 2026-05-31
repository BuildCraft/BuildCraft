/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.misc;

import javax.annotation.Nonnull;

// javax.vecmath.* → org.joml.* (Tuple3f/Vector3f → Vector3f)
import org.joml.Vector3f;

// Yarn 1.20.1: net.minecraft.util.Direction → net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/** Class for dealing with {@link Vec3d}, {@link Vec3i}, {@link Direction}, {@link Axis} conversions and additions.
 * This is for simple functions ONLY, {@link PositionUtil} is for complex interactions */
public class VecUtil {
    public static final BlockPos POS_ONE = new BlockPos(1, 1, 1);
    public static final Vec3d VEC_HALF = new Vec3d(0.5, 0.5, 0.5);
    public static final Vec3d VEC_ONE = new Vec3d(1, 1, 1);

    public static Vec3d add(Vec3d a, Vec3i b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    public static Vec3d offset(Vec3d from, Direction direction, double by) {
        return from.add(direction.getOffsetX() * by, direction.getOffsetY() * by, direction.getOffsetZ() * by);
    }

    public static double dot(Vec3d a, Vec3d b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vec3d scale(Vec3d vec, double scale) {
        return vec.multiply(scale);
    }

    public static Direction getFacing(Axis axis, boolean positive) {
        AxisDirection dir = positive ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        return Direction.get(dir, axis);
    }

    public static BlockPos absolute(BlockPos val) {
        return new BlockPos(Math.abs(val.getX()), Math.abs(val.getY()), Math.abs(val.getZ()));
    }

    public static Vec3d replaceValue(Vec3d old, Axis axis, double with) {
        return new Vec3d(//
            axis == Axis.X ? with : old.x,//
            axis == Axis.Y ? with : old.y,//
            axis == Axis.Z ? with : old.z//
        );
    }

    @Nonnull
    public static BlockPos replaceValue(Vec3i old, Axis axis, int with) {
        return new BlockPos(//
            axis == Axis.X ? with : old.getX(),//
            axis == Axis.Y ? with : old.getY(),//
            axis == Axis.Z ? with : old.getZ()//
        );
    }

    public static double getValue(Vec3d from, Axis axis) {
        return axis == Axis.X ? from.x : axis == Axis.Y ? from.y : from.z;
    }

    public static int getValue(Vec3i from, Axis axis) {
        return axis == Axis.X ? from.getX() : axis == Axis.Y ? from.getY() : from.getZ();
    }

    public static double getValue(Vec3d negative, Vec3d positive, Direction face) {
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

    public static int getValue(Vec3i negative, Vec3i positive, Direction face) {
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

    public static Vec3d convertCenter(Vec3i pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static BlockPos convertFloor(Vec3d vec) {
        return new BlockPos((int) Math.floor(vec.x), (int) Math.floor(vec.y), (int) Math.floor(vec.z));
    }

    public static BlockPos convertCeiling(Vec3d vec) {
        return new BlockPos((int) Math.ceil(vec.x), (int) Math.ceil(vec.y), (int) Math.ceil(vec.z));
    }

    public static Vector3f convertFloat(Vec3d vec) {
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

    public static Vec3d min(Vec3d a, Vec3d b) {
        if (a == null) return b;
        if (b == null) return a;
        return new Vec3d(//
            Math.min(a.x, b.x),//
            Math.min(a.y, b.y),//
            Math.min(a.z, b.z)//
        );
    }

    public static Vec3d min(Vec3d a, Vec3d b, Vec3d c) {
        return min(min(a, b), c);
    }

    public static Vec3d min(Vec3d a, Vec3d b, Vec3d c, Vec3d d) {
        return min(min(a, b), min(c, d));
    }

    public static Vec3d max(Vec3d a, Vec3d b) {
        if (a == null) return b;
        if (b == null) return a;
        return new Vec3d(//
            Math.max(a.x, b.x),//
            Math.max(a.y, b.y),//
            Math.max(a.z, b.z)//
        );
    }

    public static Vec3d max(Vec3d a, Vec3d b, Vec3d c) {
        return max(max(a, b), c);
    }

    public static Vec3d max(Vec3d a, Vec3d b, Vec3d c, Vec3d d) {
        return max(max(a, b), max(c, d));
    }
}
