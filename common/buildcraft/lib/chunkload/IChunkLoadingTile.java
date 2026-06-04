/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.chunkload;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

// Forge→Fabric migration notes (R.Chen):
//   BlockEntity                → BlockEntity
//   Direction.HORIZONTALS    → Direction.Type.HORIZONTAL
//   The BCLibConfig.ChunkLoaderLevel gating referenced in the old javadoc is deferred until BCLibConfig
//   lands; see ChunkLoaderManager.canLoadFor for the temporary always-permit behaviour.
/** This should be implemented by {@link BlockEntity}s that wish to be chunk-loaded by buildcraft lib. Note that tiles
 * should add themselves to the chunk-loading list via {@link ChunkLoaderManager#loadChunksForTile}. */
public interface IChunkLoadingTile {
    /** @return The chunkloading type, or null if this tile doesn't want to be chunkloaded. */
    @Nullable
    default LoadType getLoadType() {
        return LoadType.SOFT;
    }

    /** Gets a list of all the ADDITIONAL chunks to load.
     *
     * The default implementation returns neighbouring chunks if this block is on a chunk boundary.
     *
     * @return A set of all the additional chunks to load, optionally including the {@link ChunkPos} that this tile is
     *         contained within. If the return value is null then only the chunk containing this block will be
     *         chunkloaded. */
    @Nullable
    default Set<ChunkPos> getChunksToLoad() {
        BlockPos pos = ((BlockEntity) this).getPos();
        Set<ChunkPos> chunkPoses = new HashSet<>(4);
        for (Direction face : Direction.Type.HORIZONTAL) {
            chunkPoses.add(new ChunkPos(pos.offset(face)));
        }
        return chunkPoses;
    }

    public enum LoadType {
        /** Softly attempt to chunkload this. Under the (deferred) config gating, STRICT/NONE levels would skip it. */
        SOFT,
        /** Aggressively chunkload this. Generally only enabled for machines designed to operate far from a player's
         * territory, like a quarry or a pump. Skipped only when chunk loading is globally disabled. */
        HARD
    }
}
