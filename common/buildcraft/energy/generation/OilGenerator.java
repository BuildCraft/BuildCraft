package buildcraft.energy.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;

import net.minecraftforge.fml.common.IWorldGenerator;

import buildcraft.lib.misc.data.Box;

import buildcraft.energy.generation.OilPopulate.GenType;

public enum OilGenerator implements IWorldGenerator {
    INSTANCE;

    private static final int MAX_CHUNK_RADIUS = 5;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator gen,
        IChunkProvider provider) {
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        BlockPos min = new BlockPos(x, 0, z);
        Box box = new Box(min, min.add(15, world.getHeight(), 15));
        for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++) {
            for (int cdz = -MAX_CHUNK_RADIUS; cdz <= MAX_CHUNK_RADIUS; cdz++) {
                int cx = chunkX + cdx;
                int cz = chunkZ + cdz;
                for (OilGenStructure struct : getStructuresFor(world, cx, cz)) {
                    struct.generate(world, box);
                }
            }
        }
    }

    public static List<OilGenStructure> getStructuresFor(World world, int cx, int cz) {
        // Ensure we have the same seed for the sane chunk
        long worldSeed = world.getSeed();
        Random worldRandom = new Random(worldSeed);
        long xSeed = worldRandom.nextLong() >> 2 + 1L;
        long zSeed = worldRandom.nextLong() >> 2 + 1L;
        long chunkSeed = (xSeed * cx + zSeed * cz) ^ worldSeed;
        Random rand = new Random(chunkSeed);

        // shift to world coordinates
        int x = cx * 16 + 8 + rand.nextInt(16);
        int z = cz * 16 + 8 + rand.nextInt(16);

        // Biome biome = world.getBiome(new BlockPos(x, 0, z));

        // TODO: Check validity of this block

        Biome biome = world.getBiome(new BlockPos(x, 0, z));

        // Do not generate oil in the End or Nether
        if (OilPopulate.INSTANCE.excludedBiomeNames.contains(biome.getBiomeName())) {
            return Collections.emptyList();
        }

        boolean oilBiome = OilPopulate.INSTANCE.surfaceDepositBiomeNames.contains(biome.getBiomeName());

        double bonus = oilBiome ? 3.0 : 1.0;
        if (OilPopulate.INSTANCE.excessiveBiomeNames.contains(biome.getBiomeName())) {
            bonus *= 30.0;
        }
        GenType type = GenType.NONE;
        if (rand.nextDouble() <= 0.0004 * bonus) {
            // 0.04%
            type = GenType.LARGE;
        } else if (rand.nextDouble() <= 0.001 * bonus) {
            // 0.1%
            type = GenType.MEDIUM;
        } else if (oilBiome && rand.nextDouble() <= 0.02 * bonus) {
            // 2%
            type = GenType.LAKE;
        }

        if (type == GenType.NONE) {
            return Collections.emptyList();
        }

        // TEMP
        List<OilGenStructure> structures = new ArrayList<>();

        if (type != GenType.LAKE) {
            // Generate a spherical cave deposit
            int wellY = 20 + rand.nextInt(10);

            int radius;
            if (type == GenType.LARGE) {
                radius = 8 + rand.nextInt(9);
            } else {
                radius = 4 + rand.nextInt(4);
            }

            structures.add(OilGenUtil.createSphere(new BlockPos(x, wellY, z), radius));

            // Generate a spout

            int height;
            if (type == GenType.LARGE) {
                radius = 1;
                height = 70 + rand.nextInt(7);
                structures.add(OilGenUtil.createTubeY(new BlockPos(x, height, z), 5 + rand.nextInt(5), 0));
            } else {
                radius = 0;
                height = 68 + rand.nextInt(7);
            }
            structures.add(OilGenUtil.createTubeY(new BlockPos(x, wellY, z), height - wellY, radius));

            // Generate a spring at the very bottom
            if (type == GenType.LARGE || rand.nextFloat() < 0.2) {
                structures.add(OilGenUtil.createTubeY(new BlockPos(x, 1, z), wellY, radius));
            }
        }

        int lakeRadius;
        int tendrilRadius;
        if (type == GenType.LARGE) {
            lakeRadius = 4;
            tendrilRadius = 25 + rand.nextInt(20);
            if (/*BCCoreConfig.debugWorldGen*/false) {
                tendrilRadius += 40;
            }
        } else {
            lakeRadius = 2;
            tendrilRadius = 5 + rand.nextInt(10);
        }
        structures.add(OilGenUtil.createTendril(new BlockPos(x, 62, z), lakeRadius, tendrilRadius, rand));

        return structures;
    }
}
