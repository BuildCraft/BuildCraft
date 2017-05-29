package buildcraft.energy.generation;

import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.energy.generation.OilGenStructure.GenByPredicate;
import buildcraft.energy.generation.OilGenStructure.ReplaceType;

public class OilGenUtil {

    /*
     * Structures
     */

    public static OilGenStructure createTubeY(BlockPos base, int height, int radius) {
        return createTube(base, height, radius, Axis.Y);
    }

    public static OilGenStructure createTubeX(BlockPos start, int length, int radius) {
        return createTube(start, length, radius, Axis.X);
    }

    public static OilGenStructure createTubeZ(BlockPos start, int length, int radius) {
        return createTube(start, length, radius, Axis.Z);
    }

    private static OilGenStructure createTube(BlockPos center, int length, int radius, Axis axis) {
        int valForAxis = VecUtil.getValue(center, axis);
        BlockPos min = VecUtil.replaceValue(center.add(-radius, -radius, -radius), axis, valForAxis);
        BlockPos max = VecUtil.replaceValue(center.add(radius, radius, radius), axis, valForAxis + length);
        double radiusSq = radius * radius + 0.01;
        int toReplace = valForAxis;
        Predicate<BlockPos> tester = p -> {
            return VecUtil.replaceValue(p, axis, toReplace).distanceSq(center) <= radiusSq;
        };
        return new GenByPredicate(new Box(min, max), ReplaceType.ALWAYS, tester);
    }

    public static OilGenStructure createSphere(BlockPos center, int radius) {
        Box box = new Box(center.add(-radius, -radius, -radius), center.add(radius, radius, radius));
        double radiusSq = radius * radius + 0.01;
        Predicate<BlockPos> tester = p -> p.distanceSq(center) <= radiusSq;
        return new GenByPredicate(box, ReplaceType.ALWAYS, tester);
    }

    public static OilGenStructure createTendril(BlockPos center, int lakeRadius, int radius, Random rand) {
        BlockPos start = center.add(-radius, 0, -radius);
        int diameter = radius * 2 + 1;
        boolean[][] pattern = new boolean[diameter][diameter];

        int x = radius;
        int z = radius;
        for (int dx = -lakeRadius; dx <= lakeRadius; dx++) {
            for (int dz = -lakeRadius; dz <= lakeRadius; dz++) {
                pattern[x + dx][z + dz] = dx * dx + dz * dz <= lakeRadius * lakeRadius;
            }
        }

        for (int w = 1; w < radius; w++) {
            float proba = (float) (radius - w + 4) / (float) (radius + 4);

            fillPatternIfProba(rand, proba, x, z + w, pattern);
            fillPatternIfProba(rand, proba, x, z - w, pattern);
            fillPatternIfProba(rand, proba, x + w, z, pattern);
            fillPatternIfProba(rand, proba, x - w, z, pattern);

            for (int i = 1; i <= w; i++) {
                fillPatternIfProba(rand, proba, x + i, z + w, pattern);
                fillPatternIfProba(rand, proba, x + i, z - w, pattern);
                fillPatternIfProba(rand, proba, x + w, z + i, pattern);
                fillPatternIfProba(rand, proba, x - w, z + i, pattern);

                fillPatternIfProba(rand, proba, x - i, z + w, pattern);
                fillPatternIfProba(rand, proba, x - i, z - w, pattern);
                fillPatternIfProba(rand, proba, x + w, z - i, pattern);
                fillPatternIfProba(rand, proba, x - w, z - i, pattern);
            }
        }

        int depth = rand.nextDouble() < 0.5 ? 1 : 2;
        return OilGenStructure.FlatPattern.create(start, ReplaceType.IS_FOR_LAKE, pattern, depth);
    }

    private static void fillPatternIfProba(Random rand, float proba, int x, int z, boolean[][] pattern) {
        if (rand.nextFloat() <= proba) {
            pattern[x][z] = isSet(pattern, x, z - 1) | isSet(pattern, x, z + 1) //
                | isSet(pattern, x - 1, z) | isSet(pattern, x + 1, z);
        }
    }

    private static boolean isSet(boolean[][] pattern, int x, int z) {
        if (x < 0 || x >= pattern.length) return false;
        if (z < 0 || z >= pattern[x].length) return false;
        return pattern[x][z];
    }
}
