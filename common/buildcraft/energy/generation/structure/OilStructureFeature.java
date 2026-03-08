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
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;

public class OilStructureFeature extends Structure<OilFeatureConfiguration> {
    public OilStructureFeature(Codec<OilFeatureConfiguration> configCodec) {
        super(configCodec);
    }

    @Override
    public IStartFactory<OilFeatureConfiguration> getStartFactory() {
        return OilGenerator::new;
    }

    @Override
    public boolean isFeatureChunk(
            ChunkGenerator generator,
            BiomeProvider biomeSource,
            long seed,
            SharedSeedRandom randIn,
            int chunkX, int chunkZ,
            Biome biome,
            ChunkPos chunkPos,
            OilFeatureConfiguration featureConfig
    ) {
//        int minHeight = context.heightAccessor().getMinBuildHeight();
//        int maxHeight = context.heightAccessor().getMaxBuildHeight();

//        int x = chunkX * 16 + 8;
//        int z = chunkZ * 16 + 8;

//        for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++)
        int cx = chunkX;
        int cz = chunkZ;

        // Chunk Rand in 1.18.2
        SharedSeedRandom rand = new SharedSeedRandom(OilGenerator.MAGIC_GEN_NUMBER);
        rand.setLargeFeatureSeed(seed, cx, cz);
        // shift to world coordinates
        int xForGen = cx * 16 + 8 + rand.nextInt(16);
        int zForGen = cz * 16 + 8 + rand.nextInt(16);

        OilGenerator.GenType type = OilGenerator.getPieceTypeByRand(rand, biome, cx, cz, xForGen, zForGen, true);
        if (type == OilGenerator.GenType.NONE) {
            return false;
        }
        featureConfig.add(chunkPos, new OilFeatureConfiguration.Info(type, rand, xForGen, zForGen));
        return true;
    }
}
