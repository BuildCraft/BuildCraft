/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package buildcraft.energy.generation.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class OilStructureFeature extends StructureFeature<OilFeatureConfiguration> {
    public OilStructureFeature(Codec<OilFeatureConfiguration> configCodec) {
        super(configCodec,
                PieceGeneratorSupplier.simple(
                        OilStructureFeature::checkLocation,
                        OilGenerator::generatePieces
                )
        );
    }

    public static boolean checkLocation(PieceGeneratorSupplier.Context<OilFeatureConfiguration> context) {
        if (!context.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) {
            return false;
        }
//        int minHeight = context.heightAccessor().getMinBuildHeight();
//        int maxHeight = context.heightAccessor().getMaxBuildHeight();
        ChunkPos chunkPos = context.chunkPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;

//        int x = chunkX * 16 + 8;
//        int z = chunkZ * 16 + 8;

//        for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++)
        int cx = chunkX;
        int cz = chunkZ;

        // Chunk Rand in 1.18.2
        WorldgenRandom rand = new WorldgenRandom(new LegacyRandomSource(OilGenerator.MAGIC_GEN_NUMBER));
        rand.setLargeFeatureSeed(context.seed(), cx, cz);
        // shift to world coordinates
        int xForGen = cx * 16 + 8 + rand.nextInt(16);
        int zForGen = cz * 16 + 8 + rand.nextInt(16);
        Biome biome = context.chunkGenerator().getNoiseBiome(
                QuartPos.fromBlock(xForGen),
                QuartPos.fromBlock(63), // Calen: 63?
                QuartPos.fromBlock(zForGen)
        ).value();
        OilGenerator.GenType type = OilGenerator.getPieceTypeByRand(rand, biome, cx, cz, xForGen, zForGen, true);
        if (type == OilGenerator.GenType.NONE) {
            return false;
        }
        context.config().add(context.chunkPos(), new OilFeatureConfiguration.Info(type, rand, xForGen, zForGen));
        return true;
    }
}
