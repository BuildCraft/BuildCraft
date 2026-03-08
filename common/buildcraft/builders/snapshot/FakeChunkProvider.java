/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.*;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

//public class FakeChunkProvider implements IChunkProvider
public class FakeChunkProvider extends AbstractChunkProvider {
    private final FakeWorld world;
    // public final Map<ChunkPos, Chunk> chunks = new HashMap<>();
    public final Map<ChunkPos, Chunk> chunks = new HashMap<>();

    // final ThreadedLevelLightEngine lightEngine;
    private final WorldLightManager lightEngine;


    public FakeChunkProvider(FakeWorld world) {
        super();
        this.world = world;

        this.lightEngine = new WorldLightManager(this, true, world.dimensionType().hasSkyLight());
    }

    @Nullable
//    public Chunk getLoadedChunk(int x, int z)
    public Chunk getLoadedChunk(int x, int z) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        if (!chunks.containsKey(chunkPos)) {
//            chunks.put(chunkPos, new Chunk(world, x, z)
            chunks.put(chunkPos, new FakeChunk(world, new ChunkPrimer(new ChunkPos(x, z), UpgradeData.EMPTY)));
        }
        return chunks.get(chunkPos);
    }

    @Nullable
    @Override
//    public Chunk provideChunk(int x, int z)
    public IChunk getChunk(int x, int z, ChunkStatus status, boolean p_62226_) {
        return getLoadedChunk(x, z);
    }

    @Override
    public WorldLightManager getLightEngine() {
        return this.lightEngine;
    }

    @Override
    public IBlockReader getLevel() {
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
