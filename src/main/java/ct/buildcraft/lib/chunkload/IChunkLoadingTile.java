/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.chunkload;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import ct.buildcraft.lib.BCLibConfig;
import ct.buildcraft.lib.BCLibConfig.ChunkLoaderLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

/** This should be implemented by {@link BlockEntity}'s that wish to be chunkloaded by buildcraft lib. Note that tiles
 * should add themselves to the chunkloading list in {@link ChunkLoaderManager#loadChunksForTile(BlockEntity)} */
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
        BlockPos pos = ((BlockEntity) this).getBlockPos();
        Set<ChunkPos> chunkPoses = new HashSet<>(4);
        for (Direction face : Direction.Plane.HORIZONTAL) {
            chunkPoses.add(new ChunkPos(pos.offset(face.getNormal())));
        }
        return chunkPoses;
    }

    public enum LoadType {
        /** Softly attempt to chunkload this. If the value of {@link BCLibConfig#chunkLoadingType} is equal to
         * {@link ChunkLoaderLevel#STRICT_TILES} or {@link ChunkLoaderLevel#NONE} then it won't be loaded. */
        SOFT,
        /** If the value of {@link BCLibConfig#chunkLoadingType} is equal to {@link ChunkLoaderLevel#NONE} then it won't
         * be loaded. Generally this should only be enabled for machines that are designed to operate far from a players
         * territory, like a quarry or a pump. */
        HARD
    }
}
