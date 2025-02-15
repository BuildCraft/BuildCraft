/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

//public class FakeChunkProvider implements IChunkProvider
public class FakeChunkProvider extends ChunkSource {
    private final FakeWorld world;
    // public final Map<ChunkPos, Chunk> chunks = new HashMap<>();
    public final Map<ChunkPos, LevelChunk> chunks = new HashMap<>();

    // final ThreadedLevelLightEngine lightEngine;
    private final LevelLightEngine lightEngine;


    public FakeChunkProvider(FakeWorld world) {
        super();
        this.world = world;

        this.lightEngine = new LevelLightEngine(this, true, world.dimensionType().hasSkyLight());
    }

    @Nullable
//    public Chunk getLoadedChunk(int x, int z)
    public LevelChunk getLoadedChunk(int x, int z) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        if (!chunks.containsKey(chunkPos)) {
//            chunks.put(chunkPos, new Chunk(world, x, z)
            chunks.put(chunkPos, new FakeChunk(world, new ChunkPos(x, z)));
        }
        return chunks.get(chunkPos);
    }

    @Nullable
    @Override
//    public Chunk provideChunk(int x, int z)
    public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean p_62226_) {
        return getLoadedChunk(x, z);
    }

    @Override
//    public boolean tick()
    public void tick(BooleanSupplier p_201913_, boolean p_201914_) {
//        return false;
    }

    @Override
    public int getLoadedChunksCount() {
        return 0;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Override
    public BlockGetter getLevel() {
        return world;
    }

    @Override
//    public String makeString()
    public String gatherStats() {
        return "fake";
    }


    @Override
//    public boolean isChunkGeneratedAt(int x, int z)
    public boolean hasChunk(int x, int z) {
        return true;
    }
}
