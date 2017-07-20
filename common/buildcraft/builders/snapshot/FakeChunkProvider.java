/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FakeChunkProvider implements IChunkProvider {
    private final FakeWorld world;
    public final Map<ChunkPos, Chunk> chunks = new HashMap<>();

    public FakeChunkProvider(FakeWorld world) {
        this.world = world;
    }

    @Nullable
    @Override
    public Chunk getLoadedChunk(int x, int z) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        if (!chunks.containsKey(chunkPos)) {
            chunks.put(chunkPos, new Chunk(world, x, z) {
                @Override
                public void generateSkylightMap() {
                }
            });
        }
        return chunks.get(chunkPos);
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        return getLoadedChunk(x, z);
    }

    @Override
    public boolean tick() {
        return false;
    }

    @Override
    public String makeString() {
        return "fake";
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return true;
    }
}
