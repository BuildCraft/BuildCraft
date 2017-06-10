/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class PositionUtil {
    @Nullable
    public static EnumFacing getDirectFacingOffset(BlockPos from, BlockPos to) {
        BlockPos diff = to.subtract(from);
        boolean x = diff.getX() != 0;
        boolean y = diff.getY() != 0;
        boolean z = diff.getZ() != 0;
        if (x && y || x && z || y && z) return null;
        if (x) return diff.getX() > 0 ? EnumFacing.EAST : EnumFacing.WEST;
        if (y) return diff.getY() > 0 ? EnumFacing.UP : EnumFacing.DOWN;
        if (z) return diff.getZ() > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
        return null;
    }

    public static Set<BlockPos> getCorners(BlockPos min, BlockPos max) {
        if (min == null || max == null) return ImmutableSet.of();
        if (min.equals(max)) return ImmutableSet.of(min);
        ImmutableSet.Builder<BlockPos> set = ImmutableSet.builder();
        set.add(min);
        set.add(new BlockPos(max.getX(), min.getY(), min.getZ()));
        set.add(new BlockPos(min.getX(), max.getY(), min.getZ()));
        set.add(new BlockPos(max.getX(), max.getY(), min.getZ()));
        set.add(new BlockPos(min.getX(), min.getY(), max.getZ()));
        set.add(new BlockPos(max.getX(), min.getY(), max.getZ()));
        set.add(new BlockPos(min.getX(), max.getY(), max.getZ()));
        set.add(max);
        return set.build();
    }

    public static boolean isOnEdge(BlockPos min, BlockPos max, BlockPos pos) {
        if (min == null || max == null || pos == null) {
            return false;
        }
        int same = 0;
        if (min.getX() == pos.getX() || max.getX() == pos.getX()) {
            same++;
        }
        if (min.getY() == pos.getY() || max.getY() == pos.getY()) {
            same++;
        }
        if (min.getZ() == pos.getZ() || max.getZ() == pos.getZ()) {
            same++;
        }
        return same >= 2;
    }

    public static boolean isNextTo(BlockPos one, BlockPos two) {
        BlockPos diff = one.subtract(two);
        boolean x = diff.getX() == 1 || diff.getX() == -1;
        boolean y = diff.getY() == 1 || diff.getY() == -1;
        if (x && y) return false;
        boolean z = diff.getZ() == 1 || diff.getZ() == -1;
        if (y && z) return false;
        return x != z;
    }

    /** Finds a rotation that {@link #rotateFacing(EnumFacing, Axis, Rotation)} will use on "from" to get "to", with a
     * given axis around. */
    public static Rotation getRotatedFacing(EnumFacing from, EnumFacing to, Axis axis) {
        if (from.getAxis() == axis || to.getAxis() == axis) {
            throw new IllegalArgumentException("Cannot rotate around " + axis + " with " + from + " and " + to);
        }
        if (from == to) {
            return Rotation.NONE;
        }
        if (from.getOpposite() == to) {
            return Rotation.CLOCKWISE_180;
        }
        if (from.rotateAround(axis) == to) {
            return Rotation.CLOCKWISE_90;
        } else {
            return Rotation.COUNTERCLOCKWISE_90;
        }
    }

    /** Rotates a given {@link EnumFacing} by the given rotation, in a given axis. This relies on the behaviour defined
     * in {@link EnumFacing#rotateAround(Axis)}. */
    public static EnumFacing rotateFacing(EnumFacing from, Axis axis, Rotation rotation) {
        if (rotation == Rotation.NONE || rotation == null) {
            return from;
        }
        if (from.getAxis() == axis) {
            return from;
        } else if (rotation == Rotation.CLOCKWISE_180) {
            return from.getOpposite();
        }

        if (rotation == Rotation.COUNTERCLOCKWISE_90) {
            // 270 is the same as 180 + 90
            // Vanilla gives us 90 for free.
            from = from.getOpposite();
        }
        return from.rotateAround(axis);
    }

    /** Rotates a given vector by the given rotation, in a given axis. This relies on the behaviour of
     * {@link #rotateFacing(EnumFacing, Axis, Rotation)}. */
    public static Vec3d rotateVec(Vec3d from, Axis axis, Rotation rotation) {
        Vec3d rotated = new Vec3d(0, 0, 0);

        double numEast = from.xCoord;
        double numUp = from.yCoord;
        double numSouth = from.zCoord;

        EnumFacing newEast = PositionUtil.rotateFacing(EnumFacing.EAST, axis, rotation);
        EnumFacing newUp = PositionUtil.rotateFacing(EnumFacing.UP, axis, rotation);
        EnumFacing newSouth = PositionUtil.rotateFacing(EnumFacing.SOUTH, axis, rotation);

        rotated = VecUtil.replaceValue(rotated, newEast.getAxis(), numEast * newEast.getAxisDirection().getOffset());
        rotated = VecUtil.replaceValue(rotated, newUp.getAxis(), numUp * newUp.getAxisDirection().getOffset());
        rotated = VecUtil.replaceValue(rotated, newSouth.getAxis(), numSouth * newSouth.getAxisDirection().getOffset());

        return rotated;
    }

    /** Rotates a given position by the given rotation, in a given axis. This relies on the behaviour of
     * {@link #rotateFacing(EnumFacing, Axis, Rotation)}. */
    public static BlockPos rotatePos(Vec3i from, Axis axis, Rotation rotation) {
        BlockPos rotated = new BlockPos(0, 0, 0);

        int numEast = from.getX();
        int numUp = from.getY();
        int numSouth = from.getZ();

        EnumFacing newEast = PositionUtil.rotateFacing(EnumFacing.EAST, axis, rotation);
        EnumFacing newUp = PositionUtil.rotateFacing(EnumFacing.UP, axis, rotation);
        EnumFacing newSouth = PositionUtil.rotateFacing(EnumFacing.SOUTH, axis, rotation);

        rotated = VecUtil.replaceValue(rotated, newEast.getAxis(), numEast * newEast.getAxisDirection().getOffset());
        rotated = VecUtil.replaceValue(rotated, newUp.getAxis(), numUp * newUp.getAxisDirection().getOffset());
        rotated = VecUtil.replaceValue(rotated, newSouth.getAxis(), numSouth * newSouth.getAxisDirection().getOffset());

        return rotated;
    }

    public static LineSkewResult findLineSkewPoint(Line line, Vec3d start, Vec3d direction) {
        double ia = 0, ib = 1;
        double da = 0, db = 0;
        double id = 0.5;
        Vec3d va, vb;

        Vec3d best = null;
        for (int i = 0; i < 10; i++) {
            Vec3d a = line.interpolate(ia);
            Vec3d b = line.interpolate(ib);
            va = closestPointOnLineToPoint(a, start, direction);
            vb = closestPointOnLineToPoint(b, start, direction);
            da = a.squareDistanceTo(va);
            db = b.squareDistanceTo(vb);
            if (da < db) {
                // We work out the square root at the end to get the actual distance
                best = a;
                ib -= id;
            } else /* if (db < da) */ {
                // We work out the square root at the end to get the actual distance
                best = b;
                ia += id;
            }
            id /= 2.0;
        }
        return new LineSkewResult(best, Math.sqrt(Math.min(da, db)));
    }

    public static class LineSkewResult {
        public final Vec3d closestPos;
        public final double distFromLine;

        public LineSkewResult(Vec3d closestPos, double distFromLine) {
            this.closestPos = closestPos;
            this.distFromLine = distFromLine;
        }
    }

    public static Vec3d closestPointOnLineToPoint(Vec3d point, Vec3d linePoint, Vec3d lineVector) {
        Vec3d v = lineVector.normalize();
        Vec3d p1 = linePoint;
        Vec3d p2 = point;

        // Its maths. Its allowed to deviate from normal naming rules.
        Vec3d p2_minus_p1 = p2.subtract(p1);
        double _dot_v = VecUtil.dot(p2_minus_p1, v);
        Vec3d _scale_v = VecUtil.scale(v, _dot_v);
        return p1.add(_scale_v);
    }

    public static class Line {
        public final Vec3d start, end;

        public Line(Vec3d start, Vec3d end) {
            this.start = start;
            this.end = end;
        }

        public static Line createLongLine(Vec3d start, Vec3d direction) {
            return new Line(start, VecUtil.scale(direction, 1024));
        }

        public Vec3d interpolate(double interp) {
            return VecUtil.scale(start, 1 - interp).add(VecUtil.scale(end, interp));
        }
    }

    /** Returns a list of all the block positions on the edge of the given box. */
    public static ImmutableList<BlockPos> getAllOnEdge(BlockPos min, BlockPos max) {
        ImmutableList.Builder<BlockPos> list = ImmutableList.builder();
        boolean addX = max.getX() != min.getX();
        boolean addY = max.getY() != min.getY();
        boolean addZ = max.getZ() != min.getZ();
        if (addX & addY & addZ) {
            return getAllOnEdgeFull(min, max);
        }
        for (int x = min.getX(); x <= max.getX(); x++) {
            list.add(new BlockPos(x, min.getY(), min.getZ()));
            if (addY) {
                list.add(new BlockPos(x, max.getY(), min.getZ()));
                if (addZ) {
                    list.add(new BlockPos(x, max.getY(), max.getZ()));
                }
            } else if (addZ) {
                list.add(new BlockPos(x, min.getY(), max.getZ()));
            }
        }
        if (addY) {
            for (int y = min.getY() + 1; y < max.getY(); y++) {
                list.add(new BlockPos(min.getX(), y, min.getZ()));
                if (addX) {
                    list.add(new BlockPos(max.getX(), y, min.getZ()));
                    if (addZ) {
                        list.add(new BlockPos(max.getX(), y, max.getZ()));
                    }
                } else if (addZ) {
                    list.add(new BlockPos(min.getX(), y, max.getZ()));
                }
            }
        }
        if (addZ) {
            for (int z = min.getZ() + 1; z < max.getZ(); z++) {
                list.add(new BlockPos(min.getX(), min.getY(), z));
                if (addX) {
                    list.add(new BlockPos(max.getX(), min.getY(), z));
                    if (addY) {
                        list.add(new BlockPos(max.getX(), max.getY(), z));
                    }
                } else {
                    list.add(new BlockPos(min.getX(), max.getY(), z));
                }
            }
        }
        return list.build();
    }

    private static ImmutableList<BlockPos> getAllOnEdgeFull(BlockPos min, BlockPos max) {
        ImmutableList.Builder<BlockPos> list = ImmutableList.builder();
        for (int x = min.getX(); x <= max.getX(); x++) {
            list.add(new BlockPos(x, min.getY(), min.getZ()));
            list.add(new BlockPos(x, max.getY(), min.getZ()));
            list.add(new BlockPos(x, max.getY(), max.getZ()));
            list.add(new BlockPos(x, min.getY(), max.getZ()));
        }
        for (int y = min.getY() + 1; y < max.getY(); y++) {
            list.add(new BlockPos(min.getX(), y, min.getZ()));
            list.add(new BlockPos(max.getX(), y, min.getZ()));
            list.add(new BlockPos(max.getX(), y, max.getZ()));
            list.add(new BlockPos(min.getX(), y, max.getZ()));
        }
        for (int z = min.getZ() + 1; z < max.getZ(); z++) {
            list.add(new BlockPos(min.getX(), min.getY(), z));
            list.add(new BlockPos(max.getX(), min.getY(), z));
            list.add(new BlockPos(max.getX(), max.getY(), z));
            list.add(new BlockPos(min.getX(), max.getY(), z));
        }
        return list.build();
    }

    /** Returns a list of all the block positions between from and to (mostly).
     * <p>
     * Does not return the "from" co-ordinate, but does include the "to" co-ordinate (provided that from does not equal
     * to) */
    public static ImmutableList<BlockPos> getAllOnPath(BlockPos from, BlockPos to) {
        ImmutableList.Builder<BlockPos> interp = ImmutableList.builder();

        final BlockPos difference = to.subtract(from);

        final int ax = Math.abs(difference.getX());
        final int ay = Math.abs(difference.getY());
        final int az = Math.abs(difference.getZ());

        int count = ax + ay + az;
        BlockPos current = from;
        final int ddx = difference.getX() > 0 ? 1 : -1;
        final int ddy = difference.getY() > 0 ? 1 : -1;
        final int ddz = difference.getZ() > 0 ? 1 : -1;

        // start from 1/2 in a block
        // (as we want to compare to the centre of blocks rather than the lower corner)
        int dx = count / 2;
        int dy = count / 2;
        int dz = count / 2;

        for (int j = 0; j < count; j++) {
            dx += ax;
            dy += ay;
            dz += az;
            boolean changed = false;
            if (dx >= count) {
                changed = true;
                dx -= count;
                current = current.add(ddx, 0, 0);
            }
            if (dy >= count) {
                changed = true;
                dy -= count;
                current = current.add(0, ddy, 0);
            }
            if (dz >= count) {
                changed = true;
                dz -= count;
                current = current.add(0, 0, ddz);
            }

            if (changed) {
                interp.add(current);
            }
        }
        return interp.build();
    }

    public static BlockPos randomBlockPos(Random rand, BlockPos size) {
        return new BlockPos(//
            rand.nextInt(size.getX()),//
            rand.nextInt(size.getY()),//
            rand.nextInt(size.getZ())//
        );
    }

    public static BlockPos randomBlockPos(Random rand, BlockPos min, BlockPos max) {
        return new BlockPos(//
            min.getX() + rand.nextInt(max.getX() - min.getX()),//
            min.getY() + rand.nextInt(max.getY() - min.getY()),//
            min.getZ() + rand.nextInt(max.getZ() - min.getZ())//
        );
    }
}
