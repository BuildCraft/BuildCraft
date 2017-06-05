/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;


public class FakeChunkProvider implements IChunkProvider {
    private final World world;
    public final Map<ChunkPos, Chunk> chunks = new HashMap<>();

    public FakeChunkProvider(World world) {
        this.world = world;
    }

    @Nullable
    @Override
    public Chunk getLoadedChunk(int x, int z) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        if (!chunks.containsKey(chunkPos)) {
            Chunk chunk = new Chunk(world, x, z) {
                @Override
                public void generateSkylightMap() {
                }

                @Override
                public boolean isEmpty() {
                    return super.isEmpty();
                }
            };
            chunk.onChunkLoad();
            chunks.put(chunkPos, chunk);
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
