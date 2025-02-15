/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkUtil {
    private static final ThreadLocal<LevelChunk> lastChunk = new ThreadLocal<>();

    public static LevelChunk getChunk(Level world, BlockPos pos, boolean force) {
        return getChunk(world, pos.getX() >> 4, pos.getZ() >> 4, force);
    }

    public static LevelChunk getChunk(Level world, ChunkPos pos, boolean force) {
        return getChunk(world, pos.x, pos.z, force);
    }

    public static LevelChunk getChunk(Level world, int x, int z, boolean force) {
        LevelChunk chunk = lastChunk.get();

        if (chunk != null) {
//            if (chunk.isLoaded())
            ChunkAccess chunkAccess = world.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
            if (chunkAccess != null) {
                if (chunk.getLevel() == world && chunk.getPos().x == x && chunk.getPos().z == z) {
                    return chunk;
                }
            } else {
                lastChunk.set(null);
            }
        }

//        if (force) {
//            chunk = world.getChunkProvider().provideChunk(x, z);
//        } else {
//            chunk = world.getChunkProvider().getLoadedChunk(x, z);
//        }
        chunk = world.getChunkSource().getChunk(x, z, force);

        if (chunk != null) {
            lastChunk.set(chunk);
        }
        return chunk;
    }
}
