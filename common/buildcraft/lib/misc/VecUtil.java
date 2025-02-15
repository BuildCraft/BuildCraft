/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

/** Class for dealing with {@link Vec3}, {@link Vec3i}, {@link Direction}, {@link Axis} conversions and additions.
 * This is for simple functions ONLY, {@link PositionUtil} is for complex interactions */
public class VecUtil {
    public static final BlockPos POS_ONE = new BlockPos(1, 1, 1);
    public static final Vec3 VEC_HALF = new Vec3(0.5, 0.5, 0.5);
    public static final Vec3 VEC_ONE = new Vec3(1, 1, 1);

    public static Vec3 add(Vec3 a, Vec3i b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    public static Vec3 offset(Vec3 from, Direction direction, double by) {
        return from.add(direction.getStepX() * by, direction.getStepY() * by, direction.getStepZ() * by);
    }

    public static double dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public static Vec3 scale(Vec3 vec, double scale) {
        return vec.scale(scale);
    }

    public static Direction getFacing(Axis axis, boolean positive) {
        AxisDirection dir = positive ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE;
        return Direction.get(dir, axis);
    }

    public static BlockPos absolute(BlockPos val) {
        return new BlockPos(Math.abs(val.getX()), Math.abs(val.getY()), Math.abs(val.getZ()));
    }

    public static Vec3 replaceValue(Vec3 old, Axis axis, double with) {
        return new Vec3(//
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

    public static double getValue(Vec3 from, Axis axis) {
        return axis == Axis.X ? from.x : axis == Axis.Y ? from.y : from.z;
    }

    public static int getValue(Vec3i from, Axis axis) {
        return axis == Axis.X ? from.getX() : axis == Axis.Y ? from.getY() : from.getZ();
    }

    public static double getValue(Vec3 negative, Vec3 positive, Direction face) {
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

    public static Vec3 convertCenter(Vec3i pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static BlockPos convertFloor(Vec3 vec) {
        return BlockPos.containing(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z));
    }

    public static BlockPos convertCeiling(Vec3 vec) {
        return BlockPos.containing(Math.ceil(vec.x), Math.ceil(vec.y), Math.ceil(vec.z));
    }

    public static Vector3f convertFloat(Vec3 vec) {
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

    public static Vec3 min(Vec3 a, Vec3 b) {
        if (a == null) return b;
        if (b == null) return a;
        return new Vec3(//
                Math.min(a.x, b.x),//
                Math.min(a.y, b.y),//
                Math.min(a.z, b.z)//
        );
    }

    public static Vec3 min(Vec3 a, Vec3 b, Vec3 c) {
        return min(min(a, b), c);
    }

    public static Vec3 min(Vec3 a, Vec3 b, Vec3 c, Vec3 d) {
        return min(min(a, b), min(c, d));
    }

    public static Vec3 max(Vec3 a, Vec3 b) {
        if (a == null) return b;
        if (b == null) return a;
        return new Vec3(//
                Math.max(a.x, b.x),//
                Math.max(a.y, b.y),//
                Math.max(a.z, b.z)//
        );
    }

    public static Vec3 max(Vec3 a, Vec3 b, Vec3 c) {
        return max(max(a, b), c);
    }

    public static Vec3 max(Vec3 a, Vec3 b, Vec3 c, Vec3 d) {
        return max(max(a, b), max(c, d));
    }

    /** {@link Vec3i#distanceSq(Vec3i)} in 1.12.2 and {@link Vec3i#distSqr(Vec3i)} in 1.18.2 returns the distance to the lower connor,
     * but {@link Vector3i#distSqr(Vector3i)} in 1.16.5 returns the distance from the lower conner to the block center.
     * What is bugjump doing??? */
    public static double distanceSq(Vec3i pos1, Vec3i pos2) {
        double d1 = ((double) pos1.getX()) - (double) pos2.getX();
        double d2 = ((double) pos1.getY()) - (double) pos2.getY();
        double d3 = ((double) pos1.getZ()) - (double) pos2.getZ();
        return d1 * d1 + d2 * d2 + d3 * d3;
    }
}
