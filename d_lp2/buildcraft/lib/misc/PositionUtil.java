package buildcraft.lib.misc;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public class PositionUtil {
    /** @return The axis that connects the two positions, or null if they do not occupy the same axis-position. Also
     *         returns null if they are the same. */
    public static Axis getAxisDifference(BlockPos from, BlockPos to) {
        BlockPos diff = to.subtract(from);
        boolean x = diff.getX() == 0;
        boolean y = diff.getY() == 0;
        boolean z = diff.getZ() == 0;
        if (!x && y && z) return Axis.X;
        if (x && !y && z) return Axis.Y;
        if (x && y && !z) return Axis.Z;
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
}
