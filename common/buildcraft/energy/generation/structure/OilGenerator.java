package buildcraft.energy.generation.structure;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.generation.structure.OilGenStructurePart.GenByPredicate;
import buildcraft.energy.generation.structure.OilGenStructurePart.ReplaceType;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class OilGenerator {
    /** Random number, used to differentiate generators */
    public static final long MAGIC_GEN_NUMBER = 0xD0_46_B4_E4_0C_7D_07_CFL;

    /** The distance that oil generation will be checked to see if their structures overlap with the currently
     * generating chunk. This should be large enough that all oil generation can fit inside this radius. If this number
     * is too big then oil generation will be slightly slower */
    public static final int MAX_CHUNK_RADIUS = 5;

    public static final boolean DEBUG_OILGEN_BASIC = BCDebugging.shouldDebugLog("energy.oilgen");
    public static final boolean DEBUG_OILGEN_ALL = BCDebugging.shouldDebugComplex("energy.oilgen");

    public enum GenType {
        LARGE,
        MEDIUM,
        LAKE,
        NONE
    }

    public static void generatePieces(StructurePiecesBuilder piecesBuilder, PieceGenerator.Context<OilFeatureConfiguration> context) {
        int minHeight = context.heightAccessor().getMinBuildHeight();
        int maxHeight = context.heightAccessor().getMaxBuildHeight();
        ChunkPos chunkPos = context.chunkPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;

        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        // Calen: moved to OilGenStructureFeature#checkLocation
////        for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++)
//        int cx = chunkX;
//        int cz = chunkZ;
//
//        WorldgenRandom rand = new WorldgenRandom(new LegacyRandomSource(MAGIC_GEN_NUMBER));
//        rand.setLargeFeatureSeed(context.seed(), cx, cz);
//        // shift to world coordinates
//        int xForGen = cx * 16 + 8 + rand.nextInt(16);
//        int zForGen = cz * 16 + 8 + rand.nextInt(16);
//        Biome biome = context.chunkGenerator().getNoiseBiome(
//                QuartPos.fromBlock(xForGen),
//                QuartPos.fromBlock(63), // Calen: 63?
//                QuartPos.fromBlock(zForGen)
//        ).value();
//        GenType type = getPieceTypeByRand(rand, biome, cx, cz, xForGen, zForGen, true);
        OilFeatureConfiguration.Info info = context.config().get(chunkPos);
        if (info == null) {
            BCLog.logger.error("[energy.oilgen] Tried to gen structure pieces in chunk [" + chunkPos + "], but found none prepared data for this chunk. Something works wrong.");
            return;
        }
        GenType type = info.type;
        WorldgenRandom rand = info.oilRand;
        int xForGen = info.xForGen;
        int zForGen = info.zForGen;
        // 1.18.2: the Box required by StructurePiece
        BlockPos min = new BlockPos(x - 16 * MAX_CHUNK_RADIUS, minHeight, z - 16 * MAX_CHUNK_RADIUS);
        Box box = new Box(min, min.offset(2 * 16 * MAX_CHUNK_RADIUS, maxHeight - minHeight, 2 * 16 * MAX_CHUNK_RADIUS));

        OilStructure structure = createStructureByType(type, rand, xForGen, zForGen, minHeight, maxHeight, box);
        // type == NONE -> null
        if (structure != null) {
            piecesBuilder.addPiece(structure);
        }
    }

    /** To find out which type to gen
     * {@link GenType#NONE} means skipped and nothing for gen */
    @Nonnull
    public static GenType getPieceTypeByRand(Random rand, Biome biome, int cx, int cz, int x, int z, boolean log) {
        ResourceLocation biomeRegistryName = biome.getRegistryName();
        // Do not generate oil in excluded biomes
        boolean isExcludedBiome = BCEnergyConfig.excludedBiomes.contains(biomeRegistryName);
        if (isExcludedBiome == BCEnergyConfig.excludedBiomesIsBlackList) {
            if (DEBUG_OILGEN_BASIC & log) {
                BCLog.logger.info(
                        "[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz
                                + " because the biome we found (" + biomeRegistryName + ") is disabled!"
                );
            }
            return GenType.NONE;
        }

        if (ForgeRegistries.BIOMES.tags().getTag(Tags.Biomes.IS_END).contains(biome) && (Math.abs(x) < 1200 || Math.abs(z) < 1200)) {
            if (DEBUG_OILGEN_BASIC & log) {
                BCLog.logger.info(
                        "[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz
                                + " because it's the end biome and we're within 1200 blocks of the ender dragon fight"
                );
            }
            return GenType.NONE;
        }

        boolean oilBiome = BCEnergyConfig.surfaceDepositBiomes.contains(biomeRegistryName);

        double bonus = oilBiome ? 3.0 : 1.0;
        bonus *= BCEnergyConfig.oilWellGenerationRate;
        if (BCEnergyConfig.excessiveBiomes.contains(biomeRegistryName)) {
            bonus *= 30.0;
        }
        final GenType type;

        double nextDouble = rand.nextDouble();

        if (nextDouble <= BCEnergyConfig.largeOilGenProb * bonus) {
            // 0.04%
            type = GenType.LARGE;
        } else if (nextDouble <= BCEnergyConfig.mediumOilGenProb * bonus) {
            // 0.1%
            type = GenType.MEDIUM;
        } else if (oilBiome && nextDouble <= BCEnergyConfig.smallOilGenProb * bonus) {
            // 2%
            type = GenType.LAKE;
        } else {
            if (DEBUG_OILGEN_ALL & log) {
                BCLog.logger.info(
                        "[energy.oilgen] Not generating oil in chunk " + cx + ", " + cz
                                + " because none of the random numbers were above the thresholds for generation"
                );
            }
            type = GenType.NONE;
        }
        if (DEBUG_OILGEN_BASIC & log) {
            BCLog.logger.info(
                    "[energy.oilgen] Generating an oil well (" + type.name().toLowerCase(Locale.ROOT)
                            + ") in chunk " + cx + ", " + cz + " at " + x + ", " + z
            );
        }
        return type;
    }

    public static OilStructure createStructureByType(final GenType type, Random rand, int x, int z, int worldBottomHeight, int worldTopHeight, Box box) {
        List<OilGenStructurePart> structures = new ArrayList<>();
        final int lakeRadius;
        final int tendrilRadius;
        switch (type) {
            case LARGE:
                lakeRadius = 4;
                tendrilRadius = 25 + rand.nextInt(20);
                break;
            case MEDIUM:
                lakeRadius = 2;
                tendrilRadius = 5 + rand.nextInt(10);
                break;
            case LAKE:
                lakeRadius = 6;
                tendrilRadius = 25 + rand.nextInt(20);
                break;
            default:
                return null;
        }
        structures.add(createTendril(new BlockPos(x, 62, z), lakeRadius, tendrilRadius, rand));

        int maxHeight, minHeight;

        if (type != GenType.LAKE) {
            // Generate a spherical cave deposit
            int wellY = worldBottomHeight + 20 + rand.nextInt(10);

            int radius;
            if (type == GenType.LARGE) {
                radius = 8 + rand.nextInt(9);
            } else {
                radius = 4 + rand.nextInt(4);
            }

            structures.add(createSphere(new BlockPos(x, wellY, z), radius));

            // Generate a spout
            if (BCEnergyConfig.enableOilSpouts) {
                if (type == GenType.LARGE) {
                    minHeight = BCEnergyConfig.largeSpoutMinHeight;
                    maxHeight = BCEnergyConfig.largeSpoutMaxHeight;
                    radius = 1;
                } else {
                    minHeight = BCEnergyConfig.smallSpoutMinHeight;
                    maxHeight = BCEnergyConfig.smallSpoutMaxHeight;
                    radius = 0;
                }
                final int height;
                if (maxHeight == minHeight) {
                    height = maxHeight;
                } else {
                    if (maxHeight < minHeight) {
                        int t = maxHeight;
                        maxHeight = minHeight;
                        minHeight = t;
                    }
                    height = minHeight + rand.nextInt(maxHeight - minHeight);
                }
                structures.add(createSpout(new BlockPos(x, wellY, z), height, radius));
            }

            // Generate a spring at the very bottom
            if (type == GenType.LARGE) {
                structures.add(createTube(new BlockPos(x, worldBottomHeight + 2, z), wellY - worldBottomHeight + 1, radius, Direction.Axis.Y));
//                if (BCCoreBlocks.spring != null)
                if (BCCoreBlocks.springOil != null) {
                    structures.add(createSpring(new BlockPos(x, worldBottomHeight + 1, z)));
                }
            }
        }
        return new OilStructure(box, structures);
    }

    public static OilGenStructurePart createSpout(BlockPos start, int height, int radius) {
        return new OilGenStructurePart.Spout(start, OilGenStructurePart.ReplaceType.ALWAYS, radius, height);
    }

    public static OilGenStructurePart createTubeY(BlockPos base, int height, int radius) {
        return createTube(base, height, radius, Axis.Y);
    }

    public static OilGenStructurePart createSpring(BlockPos at) {
        return new OilGenStructurePart.Spring(at);
    }

    public static OilGenStructurePart createTube(BlockPos center, int length, int radius, Axis axis) {
        int valForAxis = VecUtil.getValue(center, axis);
        BlockPos min = VecUtil.replaceValue(center.offset(-radius, -radius, -radius), axis, valForAxis);
        BlockPos max = VecUtil.replaceValue(center.offset(radius, radius, radius), axis, valForAxis + length);
        double radiusSq = radius * radius;
        int toReplace = valForAxis;
//        Predicate<BlockPos> tester = p -> VecUtil.replaceValue(p, axis, toReplace).distSqr(center) <= radiusSq;
//        return new GenByPredicate(new Box(min, max), ReplaceType.ALWAYS, tester);
        return new GenByPredicate(new Box(min, max), ReplaceType.ALWAYS, new Object[] { axis, toReplace, center, radiusSq });
    }

    public static OilGenStructurePart createSphere(BlockPos center, int radius) {
        Box box = new Box(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius));
        double radiusSq = radius * radius + 0.01;
//        Predicate<BlockPos> tester = p -> p.distSqr(center) <= radiusSq;
//        return new OilStructurePiece.GenByPredicate(box, OilStructurePiece.ReplaceType.ALWAYS, tester);
        return new OilGenStructurePart.GenByPredicate(box, OilGenStructurePart.ReplaceType.ALWAYS, new Object[] { center, radiusSq });
    }

    public static OilGenStructurePart createTendril(BlockPos center, int lakeRadius, int radius, Random rand) {
        BlockPos start = center.offset(-radius, 0, -radius);
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
        return OilGenStructurePart.PatternTerrainHeight.create(start, OilGenStructurePart.ReplaceType.IS_FOR_LAKE, pattern, depth);
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
