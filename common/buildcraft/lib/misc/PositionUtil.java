/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.misc.data.FaceDistance;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class PositionUtil {
    /** @return The exact direction from the first position to the second. Returns null if more than one axis value is
     *         different, or they are the same position. */
    @Nullable
    public static Direction getDirectFacingOffset(BlockPos from, BlockPos to) {
        BlockPos diff = to.subtract(from);
        boolean x = diff.getX() != 0;
        boolean y = diff.getY() != 0;
        boolean z = diff.getZ() != 0;
        if (x && y || x && z || y && z) return null;
        if (x) return diff.getX() > 0 ? Direction.EAST : Direction.WEST;
        if (y) return diff.getY() > 0 ? Direction.UP : Direction.DOWN;
        if (z) return diff.getZ() > 0 ? Direction.SOUTH : Direction.NORTH;
        return null;
    }

    /** @return An integer representing the offset between the block positions, or null if
     *         {@link #getDirectFacingOffset(BlockPos, BlockPos)} returned null. The distance will be negative if
     *         returned {@link Direction} is negative. */
    @Nullable
    public static Integer getDirectFacingDistance(BlockPos from, BlockPos to) {
        BlockPos diff = to.subtract(from);
        boolean x = diff.getX() != 0;
        boolean y = diff.getY() != 0;
        boolean z = diff.getZ() != 0;
        if (x && y || x && z || y && z) return null;
        if (x) return diff.getX();
        if (y) return diff.getY();
        if (z) return diff.getZ();
        return null;
    }

    @Nullable
    public static FaceDistance getDirectOffset(BlockPos from, BlockPos to) {
        BlockPos diff = to.subtract(from);
        boolean x = diff.getX() != 0;
        boolean y = diff.getY() != 0;
        boolean z = diff.getZ() != 0;
        if (x && y || x && z || y && z) return null;
        if (x) return new FaceDistance(Axis.X, diff.getX());
        if (y) return new FaceDistance(Axis.Y, diff.getY());
        if (z) return new FaceDistance(Axis.Z, diff.getZ());
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

    private static int getBoxAxisCount(BlockPos min, BlockPos max, BlockPos pos) {
        if (min == null || max == null || pos == null) {
            return 0;
        }
        int same = 0;

        int x = pos.getX();
        int minX = min.getX();
        int maxX = max.getX();
        if (minX == x || maxX == x) {
            same++;
        } else if (minX > x || maxX < x) {
            return 0;
        }

        int y = pos.getY();
        int minY = min.getY();
        int maxY = max.getY();
        if (minY == y || maxY == y) {
            same++;
        } else if (minY > y || maxY < y) {
            return 0;
        }

        int z = pos.getZ();
        int minZ = min.getZ();
        int maxZ = max.getZ();
        if (minZ == z || maxZ == z) {
            same++;
        } else if (minZ > z || maxZ < z) {
            return 0;
        }
        return same;
    }

    /** Checks to see if the given position is a corner for the box given by min and max
     *
     * @param min The minimum co-ordinate of the box
     * @param max The maximum co-ordinate of the box
     * @param pos The position to test
     * @return True if this position was on a corner, false if not. */
    public static boolean isCorner(BlockPos min, BlockPos max, BlockPos pos) {
        return getBoxAxisCount(min, max, pos) == 3;
    }

    /** Checks to see if the given position is on one of the edges of the box given by min and max
     *
     * @param min The minimum co-ordinate of the box
     * @param max The maximum co-ordinate of the box
     * @param pos The position to test
     * @return True if this position was on an edge, false if not. */
    public static boolean isOnEdge(BlockPos min, BlockPos max, BlockPos pos) {
        return getBoxAxisCount(min, max, pos) >= 2;
    }

    /** Checks to see if the given position is on one of the faces of the box given by min and max
     *
     * @param min The minimum co-ordinate of the box
     * @param max The maximum co-ordinate of the box
     * @param pos The position to test
     * @return True if this position was on a face, false if not. */
    public static boolean isOnFace(BlockPos min, BlockPos max, BlockPos pos) {
        return getBoxAxisCount(min, max, pos) >= 1;
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

    /** Finds a rotation that {@link #rotateFacing(Direction, Axis, Rotation)} will use on "from" to get "to", with a
     * given axis around. */
    public static Rotation getRotatedFacing(Direction from, Direction to, Axis axis) {
        if (from.getAxis() == axis || to.getAxis() == axis) {
            throw new IllegalArgumentException("Cannot rotate around " + axis + " with " + from + " and " + to);
        }
        if (from == to) {
            return Rotation.NONE;
        }
        if (from.getOpposite() == to) {
            return Rotation.CLOCKWISE_180;
        }
//        if (from.rotateAround(axis) == to)
        if (RotationUtil.rotateAround(from, axis) == to) {
            return Rotation.CLOCKWISE_90;
        } else {
            return Rotation.COUNTERCLOCKWISE_90;
        }
    }

    /** Rotates a given {@link Direction} by the given rotation, in a given axis. This relies on the behaviour defined
     * in {@link RotationUtil#rotateAround(Direction, Axis)}. */
    public static Direction rotateFacing(Direction from, Axis axis, Rotation rotation) {
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
//        return from.rotateAround(axis);
        return RotationUtil.rotateAround(from, axis);
    }

    /** Rotates a given vector by the given rotation, in a given axis. This relies on the behaviour of
     * {@link #rotateFacing(Direction, Axis, Rotation)}. */
    public static Vector3d rotateVec(Vector3d from, Axis axis, Rotation rotation) {
        Vector3d rotated = new Vector3d(0, 0, 0);

        double numEast = from.x;
        double numUp = from.y;
        double numSouth = from.z;

        Direction newEast = PositionUtil.rotateFacing(Direction.EAST, axis, rotation);
        Direction newUp = PositionUtil.rotateFacing(Direction.UP, axis, rotation);
        Direction newSouth = PositionUtil.rotateFacing(Direction.SOUTH, axis, rotation);

        rotated = VecUtil.replaceValue(rotated, newEast.getAxis(), numEast * newEast.getAxisDirection().getStep());
        rotated = VecUtil.replaceValue(rotated, newUp.getAxis(), numUp * newUp.getAxisDirection().getStep());
        rotated = VecUtil.replaceValue(rotated, newSouth.getAxis(), numSouth * newSouth.getAxisDirection().getStep());

        return rotated;
    }

    /** Rotates a given position by the given rotation, in a given axis. This relies on the behaviour of
     * {@link #rotateFacing(Direction, Axis, Rotation)}. */
    public static BlockPos rotatePos(Vector3i from, Axis axis, Rotation rotation) {
        BlockPos rotated = new BlockPos(0, 0, 0);

        int numEast = from.getX();
        int numUp = from.getY();
        int numSouth = from.getZ();

        Direction newEast = PositionUtil.rotateFacing(Direction.EAST, axis, rotation);
        Direction newUp = PositionUtil.rotateFacing(Direction.UP, axis, rotation);
        Direction newSouth = PositionUtil.rotateFacing(Direction.SOUTH, axis, rotation);

        rotated = VecUtil.replaceValue(rotated, newEast.getAxis(), numEast * newEast.getAxisDirection().getStep());
        rotated = VecUtil.replaceValue(rotated, newUp.getAxis(), numUp * newUp.getAxisDirection().getStep());
        rotated = VecUtil.replaceValue(rotated, newSouth.getAxis(), numSouth * newSouth.getAxisDirection().getStep());

        return rotated;
    }

    public static LineSkewResult findLineSkewPoint(Line line, Vector3d start, Vector3d direction) {
        double ia = 0, ib = 1;
        double da = 0, db = 0;
        double id = 0.5;
        Vector3d va, vb;

        Vector3d best = null;
        for (int i = 0; i < 10; i++) {
            Vector3d a = line.interpolate(ia);
            Vector3d b = line.interpolate(ib);
            va = closestPointOnLineToPoint(a, start, direction);
            vb = closestPointOnLineToPoint(b, start, direction);
            da = a.distanceToSqr(va);
            db = b.distanceToSqr(vb);
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
        public final Vector3d closestPos;
        public final double distFromLine;

        public LineSkewResult(Vector3d closestPos, double distFromLine) {
            this.closestPos = closestPos;
            this.distFromLine = distFromLine;
        }
    }

    public static Vector3d closestPointOnLineToPoint(Vector3d point, Vector3d linePoint, Vector3d lineVector) {
        Vector3d v = lineVector.normalize();
        Vector3d p1 = linePoint;
        Vector3d p2 = point;

        // Its maths. Its allowed to deviate from normal naming rules.
        Vector3d p2_minus_p1 = p2.subtract(p1);
        double _dot_v = VecUtil.dot(p2_minus_p1, v);
        Vector3d _scale_v = VecUtil.scale(v, _dot_v);
        return p1.add(_scale_v);
    }

    public static class Line {
        public final Vector3d start, end;

        public Line(Vector3d start, Vector3d end) {
            this.start = start;
            this.end = end;
        }

        public static Line createLongLine(Vector3d start, Vector3d direction) {
            return new Line(start, VecUtil.scale(direction, 1024));
        }

        public Vector3d interpolate(double interp) {
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
            }
            if (addZ) {
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
                }
                if (addZ) {
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
                }
                if (addY) {
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

    /** Calculates the total number of blocks on the edge. This is identical to (but faster than) calling
     * {@link #getAllOnEdge(BlockPos, BlockPos)}.{@link List#size() size()}
     *
     * @return The size of the list returned by {@link #getAllOnEdge(BlockPos, BlockPos)}. */
    public static int getCountOnEdge(BlockPos min, BlockPos max) {

        int dx = Math.abs(max.getX() - min.getX());
        int dy = Math.abs(max.getY() - min.getY());
        int dz = Math.abs(max.getZ() - min.getZ());

        boolean addX = dx > 0;
        boolean addY = dy > 0;
        boolean addZ = dz > 0;

        int count = dx + 1;
        if (dy > 0) {
            count += dx + 1;
            if (addZ) {
                count += dx + 1;
            }
        }
        if (addZ) {
            count += dx + 1;
        }

        if (addY) {
            count += dy - 1;
            if (addX) {
                count += dy - 1;
                if (addZ) {
                    count += dy - 1;
                }
            }
            if (addZ) {
                count += dy - 1;
            }
        }
        if (addZ) {
            count += dz - 1;
            if (addX) {
                count += dz - 1;
                if (addY) {
                    count += dz - 1;
                }
            }
            if (addY) {
                count += dz - 1;
            }
        }
        return count;
    }

    /** Returns a list of all the block positions between from and to (mostly).
     * <p>
     * Does not return the "from" co-ordinate, but does include the "to" co-ordinate (provided that from does not equal
     * to) */
    public static ImmutableList<BlockPos> getAllOnPath(BlockPos from, BlockPos to) {
        ImmutableList.Builder<BlockPos> interp = ImmutableList.builder();

        forAllOnPath(from, to, interp::add);

        return interp.build();
    }

    public static void forAllOnPath(BlockPos from, BlockPos to, Consumer<BlockPos> iter) {

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
                current = current.offset(ddx, 0, 0);
            }
            if (dy >= count) {
                changed = true;
                dy -= count;
                current = current.offset(0, ddy, 0);
            }
            if (dz >= count) {
                changed = true;
                dz -= count;
                current = current.offset(0, 0, ddz);
            }

            if (changed) {
                iter.accept(current);
            }
        }
    }

    public static void forAllOnPath2d(int a1, int b1, int a2, int b2, PathIterator2d iter) {
        // Find the smallest number 'm' and smallest number 'o'
        // such that a * m + o = b
        // then draw a straight line (1, m)

        // First swap a with b if b is smaller than a

        int diff_a = a2 - a1;
        int diff_b = b2 - b1;

        int max_a = Math.abs(diff_a);
        int max_b = Math.abs(diff_b);

        int size_a = max_a + 1;
        int size_b = max_b + 1;

        int mult_a = diff_a > 0 ? 1 : -1;
        int mult_b = diff_b > 0 ? 1 : -1;

        boolean reverse = false;
        int multiplier;
        int offset;

        if (size_a == size_b) {
            multiplier = 1;
            offset = 0;
        } else {
            if (size_a > size_b) {
                int temp = size_a;
                size_a = size_b;
                size_b = temp;
                reverse = true;
            }
            multiplier = size_b / size_a;
            offset = size_b % size_a;
        }

        // Offset is distributed from the start of the line
        // Which is wrong atm -- need to distribute it across the line

        int normalLength = multiplier;
        int currentOffsetA = 0;
        int currentOffsetB = 0;
        int count = size_a;
        for (int i = 0; i < count; i++) {
            int length = normalLength;
            if (i < offset) {
                length++;
            }
            for (int l = 0; l < length; l++) {
                if (reverse) {
                    iter.iterate(a1 + mult_a * currentOffsetB, b1 + mult_b * currentOffsetA);
                } else {
                    iter.iterate(a1 + mult_a * currentOffsetA, b1 + mult_b * currentOffsetB);
                }
                currentOffsetB++;
            }
            currentOffsetA++;
        }
    }

    public static void forAllOnArc2d(int a, int b, int degrees, PathIterator2d iter) {

    }

    @FunctionalInterface
    public interface PathIterator2d {
        void iterate(int a, int b);
    }

    public static BlockPos randomBlockPos(Random rand, BlockPos size) {
        return new BlockPos(//
                rand.nextInt(size.getX()), //
                rand.nextInt(size.getY()), //
                rand.nextInt(size.getZ())//
        );
    }

    public static BlockPos randomBlockPos(Random rand, BlockPos min, BlockPos max) {
        return new BlockPos(//
                min.getX() + rand.nextInt(max.getX() - min.getX()), //
                min.getY() + rand.nextInt(max.getY() - min.getY()), //
                min.getZ() + rand.nextInt(max.getZ() - min.getZ())//
        );
    }
}
