/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class FakeChunkProvider extends ChunkSource {
    private final FakeWorld world;
    public final Map<ChunkPos, LevelChunk> chunks = new HashMap<>();

    public FakeChunkProvider(FakeWorld world) {
        this.world = world;
    }

    @Nullable
    @Override
    public LevelChunk getChunkNow(int x, int z) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        if (!chunks.containsKey(chunkPos)) {
            chunks.put(chunkPos, new LevelChunk(world, chunkPos));
        }
        return chunks.get(chunkPos);
    }

    @Override
    public void tick(BooleanSupplier p_202162_, boolean p_202163_) {
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return true;
    }

	@Override
	public BlockGetter getLevel() {
		return world;
	}

	@Override
	public ChunkAccess getChunk(int x, int z, ChunkStatus p_62225_, boolean p_62226_) {
		return getChunkNow(x, z);
	}

	@Override
	public String gatherStats() {
		return "fake";
	}

	@Override
	public int getLoadedChunksCount() {
		return 0;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return new LevelLightEngine(this, true, true);
	}
}
