/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.chunkload;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * Forge→Fabric chunk-loading migration (R.Chen):
 *   ForgeChunkManager.Ticket / requestTicket / forceChunk / unforceChunk
 *     → {@link ServerWorld#setChunkForced(int, int, boolean)} (vanilla force-loaded chunks, persisted by the
 *       server in the world's forced-chunk set — no manual ticket rebinding needed on world load).
 *
 * The Forge implementation tracked one ticket per tile and diffed its chunk list each update; this keeps the
 * same diff behaviour but drives it through {@code setChunkForced}. The Forge {@code rebindTickets} hook is
 * dropped because Fabric/vanilla persists forced chunks itself.
 */
public class ChunkLoaderManager {

    // Tracks the chunks currently force-loaded per tile position, so they can be diffed and released.
    // STUB(R.Chen): keyed on BlockPos only (no dimension/WorldPos) — the Forge WorldPos key and the
    // BCLibConfig.chunkLoadingLevel gating are deferred until BCLibConfig lands in libLeaf. A single quarry
    // per position makes multi-world key collisions harmless in practice.
    private static final Map<BlockPos, Set<ChunkPos>> FORCED = new HashMap<>();

    /** Should be called when a tile that wants to chunkload loads/validates. A check is performed to see if loading
     * is permitted (currently always, pending the config gating). */
    public static <T extends BlockEntity & IChunkLoadingTile> void loadChunksForTile(T tile) {
        World world = tile.getWorld();
        if (!canLoadFor(tile) || !(world instanceof ServerWorld)) {
            releaseChunksFor(tile);
            return;
        }
        updateChunksFor(tile, (ServerWorld) world);
    }

    public static <T extends BlockEntity & IChunkLoadingTile> void releaseChunksFor(T tile) {
        Set<ChunkPos> previous = FORCED.remove(tile.getPos());
        if (previous == null) {
            return;
        }
        World world = tile.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            for (ChunkPos pos : previous) {
                serverWorld.setChunkForced(pos.x, pos.z, false);
            }
        }
    }

    private static <T extends BlockEntity & IChunkLoadingTile> void updateChunksFor(T tile, ServerWorld world) {
        Set<ChunkPos> wanted = getChunksToLoad(tile);
        Set<ChunkPos> previous = FORCED.getOrDefault(tile.getPos(), Collections.emptySet());
        // Unforce chunks we no longer need.
        for (ChunkPos pos : previous) {
            if (!wanted.contains(pos)) {
                world.setChunkForced(pos.x, pos.z, false);
            }
        }
        // Force newly-wanted chunks.
        for (ChunkPos pos : wanted) {
            if (!previous.contains(pos)) {
                world.setChunkForced(pos.x, pos.z, true);
            }
        }
        FORCED.put(tile.getPos(), new HashSet<>(wanted));
    }

    public static <T extends BlockEntity & IChunkLoadingTile> Set<ChunkPos> getChunksToLoad(T tile) {
        Set<ChunkPos> chunksToLoad = tile.getChunksToLoad();
        Set<ChunkPos> chunkPoses = new HashSet<>(chunksToLoad != null ? chunksToLoad : Collections.emptySet());
        chunkPoses.add(new ChunkPos(tile.getPos()));
        return chunkPoses;
    }

    // STUB(R.Chen): Forge BCLibConfig.chunkLoadingLevel.canLoad(loadType) gating deferred (BCLibConfig not yet
    // in libLeaf). Permits any tile that declares a non-null LoadType; the NONE/STRICT config levels return.
    private static boolean canLoadFor(IChunkLoadingTile tile) {
        return tile.getLoadType() != null;
    }
}
