/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;

public class FakeBiomeProvider extends BiomeProvider {
    public static final Biome BIOME = Biomes.PLAINS;

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return Collections.emptyList();
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return BIOME;
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome defaultBiome) {
        return BIOME;
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        return biomes;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
        return oldBiomeList;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return listToReuse;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        return false;
    }

    @Nullable
    @Override
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return BlockPos.ORIGIN;
    }

    @Override
    public void cleanupCache() {
    }

    @Override
    public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
        return original;
    }

    @Override
    public boolean isFixedBiome() {
        return true;
    }

    @Override
    public Biome getFixedBiome() {
        return BIOME;
    }
}
