/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import java.io.IOException;
import javax.annotation.Nullable;

import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;

public class FakeChunkLoader implements IChunkLoader {


    @Nullable
    @Override
    public Chunk loadChunk(World worldIn, int x, int z) throws IOException {
        return worldIn.getChunkProvider().getLoadedChunk(x, z);
    }

    @Override
    public void saveChunk(World worldIn, Chunk chunkIn) throws MinecraftException, IOException {

    }

    @Override
    public void saveExtraChunkData(World worldIn, Chunk chunkIn) throws IOException {

    }

    @Override
    public void chunkTick() {

    }

    @Override
    public void saveExtraData() {

    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return true;
    }


}
