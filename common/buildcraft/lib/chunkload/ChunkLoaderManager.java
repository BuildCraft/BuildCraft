/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.chunkload;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeChunkManager;

import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.WorldPos;

public class ChunkLoaderManager {
    private static final Map<WorldPos, ForgeChunkManager.Ticket> TICKETS = new HashMap<>();

    /**
     * This should be called in {@link TileEntity#validate()}, if a tile entity might be able to load. A check is
     * performed to see if the config allows it
     */
    public static <T extends TileEntity & IChunkLoadingTile> void loadChunksForTile(@Nonnull T tile) {
        if (TICKETS.containsKey(new WorldPos(tile))) {
            updateChunksFor(tile);
            return;
        }
        ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(
            BCLib.INSTANCE,
            tile.getWorld(),
            ForgeChunkManager.Type.NORMAL
        );
        if (ticket == null) {
            BCLog.logger.warn("Chunkloading failed, most likely the limit was reached");
            return;
        }
        if (!canLoadFor(tile)) {
            return;
        }
        ticket.getModData().setTag("location", NBTUtilBC.writeBlockPos(tile.getPos()));
        for (ChunkPos chunkPos : getChunksToLoad(tile)) {
            ForgeChunkManager.forceChunk(ticket, chunkPos);
        }
        TICKETS.put(new WorldPos(tile), ticket);
    }

    public static <T extends TileEntity & IChunkLoadingTile> void releaseChunksFor(@Nonnull T tile) {
        ForgeChunkManager.releaseTicket(TICKETS.get(new WorldPos(tile)));
        TICKETS.remove(new WorldPos(tile));
    }

    private static <T extends TileEntity & IChunkLoadingTile> void updateChunksFor(@Nonnull T tile) {
        if (!TICKETS.containsKey(new WorldPos(tile))) {
            loadChunksForTile(tile);
        }
        ForgeChunkManager.Ticket ticket = TICKETS.get(new WorldPos(tile));
        Set<ChunkPos> chunks = getChunksToLoad(tile);
        for (ChunkPos pos : ticket.getChunkList()) {
            if (!chunks.contains(pos)) {
                ForgeChunkManager.unforceChunk(ticket, pos);
            }
        }
        for (ChunkPos pos : chunks) {
            if (!ticket.getChunkList().contains(pos)) {
                ForgeChunkManager.forceChunk(ticket, pos);
            }
        }
    }

    public static <T extends TileEntity & IChunkLoadingTile> Set<ChunkPos> getChunksToLoad(@Nonnull T tile) {
        Set<ChunkPos> chunksToLoad = tile.getChunksToLoad();
        Set<ChunkPos> chunkPoses = new HashSet<>(chunksToLoad != null ? chunksToLoad : Collections.emptyList());
        chunkPoses.add(new ChunkPos(tile.getPos()));
        return chunkPoses;
    }

    public static void rebindTickets(List<ForgeChunkManager.Ticket> tickets, World world) {
        TICKETS.clear();
        if (BCLibConfig.chunkLoadingLevel != BCLibConfig.ChunkLoaderLevel.NONE) {
            for (ForgeChunkManager.Ticket ticket : tickets) {
                BlockPos pos = NBTUtilBC.readBlockPos(ticket.getModData().getTag("location"));
                if (pos == null) {
                    ForgeChunkManager.releaseTicket(ticket);
                    continue;
                }
                if (TICKETS.containsKey(new WorldPos(world, pos))) {
                    ForgeChunkManager.releaseTicket(ticket);
                    continue;
                }
                TICKETS.put(new WorldPos(world, pos), ticket);
                TileEntity tile = world.getTileEntity(pos);
                if (tile == null || !(tile instanceof IChunkLoadingTile) || !canLoadFor((IChunkLoadingTile) tile)) {
                    TICKETS.remove(new WorldPos(world, pos));
                    ForgeChunkManager.releaseTicket(ticket);
                    continue;
                }
                for (ChunkPos chunkPos : getChunksToLoad((TileEntity & IChunkLoadingTile) tile)) {
                    ForgeChunkManager.forceChunk(ticket, chunkPos);
                }
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean canLoadFor(IChunkLoadingTile tile) {
        return BCLibConfig.chunkLoadingLevel.canLoad(tile.getLoadType());
    }
}
