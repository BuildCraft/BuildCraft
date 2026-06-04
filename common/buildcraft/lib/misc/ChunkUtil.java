/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkUtil {
    public static WorldChunk getChunk(World world, BlockPos pos, boolean force) {
        return getChunk(world, pos.getX() >> 4, pos.getZ() >> 4, force);
    }

    public static WorldChunk getChunk(World world, ChunkPos pos, boolean force) {
        return getChunk(world, pos.x, pos.z, force);
    }

    public static WorldChunk getChunk(World world, int x, int z, boolean force) {
        // STUB(R.Chen): Forge world.getChunkProvider().provideChunk/getLoadedChunk + the ThreadLocal
        // last-chunk cache dropped. Yarn's ChunkManager already caches; force loads via World.getChunk.
        if (force) {
            return world.getChunk(x, z);
        }
        return world.getChunkManager().getWorldChunk(x, z);
    }
}
