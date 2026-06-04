/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

// TODO(R.Chen): FakeWorld needs full rewrite for 1.20.1 — the 1.12.2 World API (WorldProvider,
// SaveHandlerMP, WorldInfo, WorldSettings, BiomeProvider, IChunkProvider) was completely replaced.
// For now this is a stub so that ClientSnapshots.java compiles.

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.schematics.ISchematicBlock;

/**
 * STUB(R.Chen): FakeWorld — a minimal in-memory world for snapshot rendering.
 * The 1.12.2 version extended World directly; in 1.20.1 we cannot extend World
 * without a server/levelSource. This stub provides just enough API for
 * ClientSnapshots.java to compile. Full port blocked on world API redesign.
 * TODO(R.Chen): properly implement using a fake WorldAccess / BlockView.
 */
@SuppressWarnings("NullableProblems")
@Environment(EnvType.CLIENT)
public class FakeWorld {
    public static final BlockPos BLUEPRINT_OFFSET = new BlockPos(0, 127, 0);

    private final java.util.HashMap<BlockPos, BlockState> blockStates = new java.util.HashMap<>();
    private final java.util.HashMap<BlockPos, BlockEntity> blockEntities = new java.util.HashMap<>();

    public FakeWorld() {}

    public void clear() {
        blockStates.clear();
        blockEntities.clear();
    }

    public void uploadSnapshot(Snapshot snapshot) {
        for (int z = 0; z < snapshot.size.getZ(); z++) {
            for (int y = 0; y < snapshot.size.getY(); y++) {
                for (int x = 0; x < snapshot.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).add(BLUEPRINT_OFFSET);
                    if (snapshot instanceof Template) {
                        if (((Template) snapshot).data.get(snapshot.posToIndex(x, y, z))) {
                            setBlockState(pos, Blocks.QUARTZ_BLOCK.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        blockStates.put(pos, state);
    }

    public BlockState getBlockState(BlockPos pos) {
        return blockStates.getOrDefault(pos, Blocks.AIR.getDefaultState());
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return blockEntities.get(pos);
    }

    public List<? extends Entity> getEntities(Class<? extends Entity> clazz, com.google.common.base.Predicate<Entity> pred) {
        return Collections.emptyList();
    }

    public BlockPos getSpawnPoint() { return BLUEPRINT_OFFSET; }

    public boolean isClient() { return true; }
}
