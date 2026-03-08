/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.chunkload;

import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.misc.data.WorldPos;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.common.world.ForgeChunkManager.TicketHelper;

import java.util.*;

public class ChunkLoaderManager {
    // private static final Map<WorldPos, Ticket> TICKETS = new HashMap<>();
    private static final Map<WorldPos, Pair<LongSet, LongSet>> TICKETS = new HashMap<>();

    /**
     * This should be called in {@link BlockEntity#clearRemoved()}, if a tile entity might be able to load. A check is
     * performed to see if the config allows it
     */
    public static <T extends BlockEntity & IChunkLoadingTile> void loadChunksForTile(T tile) {
        if (!(tile.getLevel() instanceof ServerLevel)) {
            return;
        }
        if (!canLoadFor(tile)) {
            releaseChunksFor(tile);
            return;
        }
        updateChunksFor(tile);
    }

    public static <T extends BlockEntity & IChunkLoadingTile> void releaseChunksFor(T tile) {
        if (!(tile.getLevel() instanceof ServerLevel)) {
            return;
        }
//        ForgeChunkManager.releaseTicket(TICKETS.remove(new WorldPos(tile)));
        Pair<LongSet, LongSet> removed = TICKETS.remove(new WorldPos(tile));
        // Calen: [Server thread/ERROR]: Failed to save chunk [-27, -1]
        // NullPointerException: Cannot invoke "com.mojang.datafixers.util.Pair.getSecond()" because "removed" is null
        if (removed != null) {
            removed.getSecond().forEach(cp -> unforceChunk((ServerLevel) tile.getLevel(), tile.getBlockPos(), new ChunkPos(cp)));
        }
    }

    private static <T extends BlockEntity & IChunkLoadingTile> void updateChunksFor(T tile) {
        if (!(tile.getLevel() instanceof ServerLevel)) {
            return;
        }
        WorldPos wPos = new WorldPos(tile);
//        Ticket ticket = TICKETS.get(wPos);
        Pair<LongSet, LongSet> ticket = TICKETS.get(wPos);
        if (ticket == null) {
//            ticket = ForgeChunkManager.requestTicket(
//                    BCLib.INSTANCE,
//                    tile.getLevel(),
//                    ForgeChunkManager.Type.NORMAL
//            );
            ticket = new Pair<>(new LongOpenHashSet(), new LongOpenHashSet());
//            if (ticket == null) {
//                BCLog.logger.warn("[lib.chunkloading] Failed to chunkload " + tile.getClass().getName() + " at " + tile.getBlockPos());
//                return;
//            }
//            ticket.getModData().setTag("location", NBTUtilBC.writeBlockPos(tile.getBlockPos()));
            TICKETS.put(wPos, ticket);
        }
        Set<ChunkPos> chunks = getChunksToLoad(tile);
        // Calen: unload invalid chunks
//        for (ChunkPos pos : ticket.getChunkList())
        for (Long pos : ticket.getSecond()) {
            if (!chunks.contains(new ChunkPos(pos))) {
//                ForgeChunkManager.unforceChunk(ticket, pos);
                unforceChunk((ServerLevel) tile.getLevel(), tile.getBlockPos(), new ChunkPos(pos));
            }
        }
        // Calen: load chunks should load but not
        for (ChunkPos pos : chunks) {
//            if (!ticket.getChunkList().contains(pos))
            if (!ticket.getSecond().contains(pos.toLong())) {
//                ForgeChunkManager.forceChunk(ticket, pos);
                forceChunk((ServerLevel) tile.getLevel(), tile.getBlockPos(), pos);
                ticket.getSecond().add(pos.toLong());
            }
        }
    }

    // Calen
    public static boolean unforceChunk(ServerLevel world, BlockPos owner, ChunkPos chunkPos) {
//        world.getChunkSource().removeRegionTicket(TicketType.FORCED, chunkPos, 0, chunkPos);
        return ForgeChunkManager.forceChunk(world, BCLib.MODID, owner, chunkPos.x, chunkPos.z, false, true);
    }

    public static boolean forceChunk(ServerLevel world, BlockPos owner, ChunkPos chunkPos) {
//        world.getChunkSource().addRegionTicket(TicketType.FORCED, chunkPos, 0, chunkPos);
        return ForgeChunkManager.forceChunk(world, BCLib.MODID, owner, chunkPos.x, chunkPos.z, true, true);
    }

    public static <T extends BlockEntity & IChunkLoadingTile> Set<ChunkPos> getChunksToLoad(T tile) {
        Set<ChunkPos> chunksToLoad = tile.getChunksToLoad();
        Set<ChunkPos> chunkPoses = new HashSet<>(chunksToLoad != null ? chunksToLoad : Collections.emptyList());
        chunkPoses.add(new ChunkPos(tile.getBlockPos()));
        return chunkPoses;
    }

    // public static void rebindTickets(List<TicketTracker> tickets, Level world)
    public static void rebindTickets(ServerLevel world, TicketHelper ticketHelper) {
        Map<BlockPos, Pair<LongSet, LongSet>> tickets = ticketHelper.getBlockTickets();
        TICKETS.clear();
        if (BCLibConfig.chunkLoadingLevel != BCLibConfig.ChunkLoaderLevel.NONE) {
//            for (TicketTracker ticket : tickets)
            for (BlockPos pos : tickets.keySet()) {
//                BlockPos pos = NBTUtilBC.readBlockPos(ticket.getModData().getTag("location"));
                if (pos == null) {
                    // Calen: should not run here, because pos should not be null
//                    ForgeChunkManager.releaseTicket(ticket);
                    ticketHelper.removeAllTickets(pos);
                    continue;
                }
                WorldPos wPos = new WorldPos(world, pos);
                if (TICKETS.containsKey(wPos)) {
                    // Calen: should not run here, because duplicated pos should not appear in Map<BlockPos, Pair<LongSet, LongSet>> tickets
                    // and should not be duplicated WorldPos added into TICKETS
//                    ForgeChunkManager.releaseTicket(ticket);
                    ticketHelper.removeAllTickets(pos);
                    continue;
                }
                BlockEntity tile = world.getBlockEntity(pos);
                if (tile == null || !(tile instanceof IChunkLoadingTile) || !canLoadFor((IChunkLoadingTile) tile)) {
                    // Calen: if the tile is no longer a chunk loader, release
                    TICKETS.remove(wPos);
//                    ForgeChunkManager.releaseTicket(ticket);
                    ticketHelper.removeAllTickets(pos);
                    continue;
                }
//                TICKETS.put(wPos, ticket);
                TICKETS.put(wPos, tickets.get(pos));
                for (ChunkPos chunkPos : getChunksToLoad((BlockEntity & IChunkLoadingTile) tile)) {
//                    ForgeChunkManager.forceChunk(ticket, chunkPos);
                    ForgeChunkManager.forceChunk(world, BCLib.MODID, tile.getBlockPos(), chunkPos.x, chunkPos.z, true, true);
                }
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean canLoadFor(IChunkLoadingTile tile) {
        return BCLibConfig.chunkLoadingLevel.canLoad(tile.getLoadType());
    }
}
