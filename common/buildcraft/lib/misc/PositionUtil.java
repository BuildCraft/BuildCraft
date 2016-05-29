package buildcraft.lib.misc;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

    public static boolean isNextTo(BlockPos one, BlockPos two) {
        BlockPos diff = one.subtract(two);
        boolean x = diff.getX() == 1 || diff.getX() == -1;
        boolean y = diff.getY() == 1 || diff.getY() == -1;
        if (x && y) return false;
        boolean z = diff.getZ() == 1 || diff.getZ() == -1;
        if (y && z) return false;
        return x != z;
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
}
