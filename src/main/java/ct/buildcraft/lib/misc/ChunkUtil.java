/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ChunkUtil {
    private static final ThreadLocal<LevelChunk> lastChunk = new ThreadLocal<>();

    public static LevelChunk getChunk(Level Level, BlockPos pos, boolean force) {
        return getChunk(Level, pos.getX() >> 4, pos.getZ() >> 4, force);
    }

    public static LevelChunk getChunk(Level Level, ChunkPos pos, boolean force) {
        return getChunk(Level, pos.x, pos.z, force);
    }

    public static LevelChunk getChunk(Level Level, int x, int z, boolean force) {
        LevelChunk chunk = lastChunk.get();

        if (chunk != null) {
            if (!chunk.isEmpty()) {
                if (chunk.getLevel() == Level && chunk.getPos().x == x && chunk.getPos().z == z) {
                    return chunk;
                }
            } else {
                lastChunk.set(null);
            }
        }

        if (force) {
            chunk = Level.getChunk(x, z);
        } else {
            chunk = Level.getChunk(x, z);
        }

        if (chunk != null) {
            lastChunk.set(chunk);
        }
        return chunk;
    }
}
