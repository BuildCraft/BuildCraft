/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.schematics.ISchematicBlock;

@SuppressWarnings("NullableProblems")
@SideOnly(Side.CLIENT)
public class FakeWorld extends World {
    private static final Biome BIOME = Biomes.PLAINS;
    @SuppressWarnings("WeakerAccess")
    public static final BlockPos BLUEPRINT_OFFSET = new BlockPos(0, 127, 0);

    @SuppressWarnings("WeakerAccess")
    public FakeWorld() {
        super(
            new SaveHandlerMP(),
            new WorldInfo(
                new WorldSettings(
                    0,
                    GameType.CREATIVE,
                    true,
                    false,
                    WorldType.DEFAULT
                ),
                "fake"
            ),
            new WorldProvider() {
                @Override
                public DimensionType getDimensionType() {
                    return DimensionType.OVERWORLD;
                }
            },
            new Profiler(),
            true
        );
        chunkProvider = new FakeChunkProvider(this);
    }

    public void clear() {
        ((FakeChunkProvider) chunkProvider).chunks.clear();
    }

    @SuppressWarnings("WeakerAccess")
    public void uploadSnapshot(Snapshot snapshot) {
        for (int z = 0; z < snapshot.size.getZ(); z++) {
            for (int y = 0; y < snapshot.size.getY(); y++) {
                for (int x = 0; x < snapshot.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).add(BLUEPRINT_OFFSET);
                    if (snapshot instanceof Blueprint) {
                        ISchematicBlock<?> schematicBlock = ((Blueprint) snapshot).palette
                            .get(((Blueprint) snapshot).data[snapshot.posToIndex(x, y, z)]);
                        if (!schematicBlock.isAir()) {
                            schematicBlock.buildWithoutChecks(this, pos);
                        }
                    }
                    if (snapshot instanceof Template) {
                        if (((Template) snapshot).data.get(snapshot.posToIndex(x, y, z))) {
                            setBlockState(pos, Blocks.QUARTZ_BLOCK.getDefaultState());
                        }
                    }
                }
            }
        }
        if (snapshot instanceof Blueprint) {
            ((Blueprint) snapshot).entities.forEach(schematicEntity ->
                schematicEntity.buildWithoutChecks(this, FakeWorld.BLUEPRINT_OFFSET)
            );
        }
    }

    @Override
    public BlockPos getSpawnPoint() {
        return BLUEPRINT_OFFSET;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return chunkProvider;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return BIOME;
    }

    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        return BIOME;
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return new BiomeProvider(worldInfo) {
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
        };
    }
}
