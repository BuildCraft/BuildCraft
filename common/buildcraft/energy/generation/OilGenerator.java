package buildcraft.energy.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;

import net.minecraftforge.fml.common.IWorldGenerator;

import buildcraft.lib.misc.RandUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.generation.OilGenStructure.GenByPredicate;
import buildcraft.energy.generation.OilGenStructure.ReplaceType;
import buildcraft.energy.generation.OilGenStructure.Spring;

public enum OilGenerator implements IWorldGenerator {
    INSTANCE;

    /** Random number, used to differentiate generators */
    private static final long MAGIC_GEN_NUMBER = 0xD0_46_B4_E4_0C_7D_07_CFL;

    /** The distance that oil generation will be checked to see if their structures overlap with the currently
     * generating chunk. This should be large enough that all oil generation can fit inside this radius. If this number
     * is too big then oil generation will be slightly slower */
    private static final int MAX_CHUNK_RADIUS = 5;

    public enum GenType {
        LARGE,
        MEDIUM,
        LAKE,
        NONE
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator gen,
        IChunkProvider provider) {

        if (BCEnergyConfig.excludedDimensions.contains(world.provider.getDimension())) {
            return;
        }

        world.profiler.startSection("bc_oil");
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        BlockPos min = new BlockPos(x, 0, z);
        Box box = new Box(min, min.add(15, world.getHeight(), 15));

        for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++) {
            for (int cdz = -MAX_CHUNK_RADIUS; cdz <= MAX_CHUNK_RADIUS; cdz++) {
                int cx = chunkX + cdx;
                int cz = chunkZ + cdz;
                world.profiler.startSection("scan");
                List<OilGenStructure> structures = getStructures(world, cx, cz);
                OilGenStructure.Spring spring = null;
                world.profiler.endStartSection("gen");
                for (OilGenStructure struct : structures) {
                    struct.generate(world, box);
                    if (struct instanceof OilGenStructure.Spring) {
                        spring = (Spring) struct;
                    }
                }
                if (spring != null && box.contains(spring.pos)) {
                    int count = 0;
                    for (OilGenStructure struct : structures) {
                        count += struct.countOilBlocks();
                    }
                    spring.generate(world, count);
                }
                world.profiler.endSection();
            }
        }
        world.profiler.endSection();
    }

    public static List<OilGenStructure> getStructures(World world, int cx, int cz) {
        Random rand = RandUtil.createRandomForChunk(world, cx, cz, MAGIC_GEN_NUMBER);

        // shift to world coordinates
        int x = cx * 16 + 8 + rand.nextInt(16);
        int z = cz * 16 + 8 + rand.nextInt(16);

        Biome biome = world.getBiome(new BlockPos(x, 0, z));

        // Do not generate oil in the End or Nether
        if (BCEnergyConfig.excludedBiomes.contains(biome.getRegistryName())) {
            return ImmutableList.of();
        }

        boolean oilBiome = BCEnergyConfig.surfaceDepositBiomes.contains(biome.getRegistryName());

        double bonus = oilBiome ? 3.0 : 1.0;
        if (BCEnergyConfig.excessiveBiomes.contains(biome.getRegistryName())) {
            bonus *= 30.0;
        }
        final GenType type;
        if (rand.nextDouble() <= 0.0004 * bonus) {
            // 0.04%
            type = GenType.LARGE;
        } else if (rand.nextDouble() <= 0.001 * bonus) {
            // 0.1%
            type = GenType.MEDIUM;
        } else if (oilBiome && rand.nextDouble() <= 0.02 * bonus) {
            // 2%
            type = GenType.LAKE;
        } else {
            return ImmutableList.of();
        }

        List<OilGenStructure> structures = new ArrayList<>();
        int lakeRadius;
        int tendrilRadius;
        if (type == GenType.LARGE) {
            lakeRadius = 4;
            tendrilRadius = 25 + rand.nextInt(20);
        } else if (type == GenType.LAKE) {
            lakeRadius = 6;
            tendrilRadius = 25 + rand.nextInt(20);
        } else {
            lakeRadius = 2;
            tendrilRadius = 5 + rand.nextInt(10);
        }
        structures.add(createTendril(new BlockPos(x, 62, z), lakeRadius, tendrilRadius, rand));

        if (type != GenType.LAKE) {
            // Generate a spherical cave deposit
            int wellY = 20 + rand.nextInt(10);

            int radius;
            if (type == GenType.LARGE) {
                radius = 8 + rand.nextInt(9);
            } else {
                radius = 4 + rand.nextInt(4);
            }

            structures.add(createSphere(new BlockPos(x, wellY, z), radius));

            // Generate a spout

            int height;
            if (type == GenType.LARGE) {
                height = 5 + rand.nextInt(6);
                radius = 1;
            } else {
                height = 4 + rand.nextInt(4);
                radius = 0;
            }
            structures.add(createSpout(new BlockPos(x, wellY, z), height, radius));

            // Generate a spring at the very bottom
            if (type == GenType.LARGE) {
                structures.add(createTubeY(new BlockPos(x, 1, z), wellY, radius));
                structures.add(createSpring(new BlockPos(x, 0, z)));
            }
        }
        return structures;
    }

    private static OilGenStructure createSpout(BlockPos start, int height, int radius) {
        return new OilGenStructure.Spout(start, ReplaceType.ALWAYS, radius, height);
    }

    public static OilGenStructure createTubeY(BlockPos base, int height, int radius) {
        return createTube(base, height, radius, Axis.Y);
    }

    public static OilGenStructure createTubeX(BlockPos start, int length, int radius) {
        return createTube(start, length, radius, Axis.X);
    }

    public static OilGenStructure createTubeZ(BlockPos start, int length, int radius) {
        return createTube(start, length, radius, Axis.Z);
    }

    public static OilGenStructure createSpring(BlockPos at) {
        return new OilGenStructure.Spring(at);
    }

    private static OilGenStructure createTube(BlockPos center, int length, int radius, Axis axis) {
        int valForAxis = VecUtil.getValue(center, axis);
        BlockPos min = VecUtil.replaceValue(center.add(-radius, -radius, -radius), axis, valForAxis);
        BlockPos max = VecUtil.replaceValue(center.add(radius, radius, radius), axis, valForAxis + length);
        double radiusSq = radius * radius;
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
