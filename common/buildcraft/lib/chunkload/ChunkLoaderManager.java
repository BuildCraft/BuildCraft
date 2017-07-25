/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.chunkload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeChunkManager;

import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.misc.NBTUtilBC;

public class ChunkLoaderManager {
    private static final Map<IChunkLoadingTile, ForgeChunkManager.Ticket> TICKETS = new WeakHashMap<>();

    /**
     * This should be called in {@link TileEntity#validate()}, if a tile entity might be able to load. A check is
     * automatically performed to see if the config allows it
     */
    public static <T extends TileEntity & IChunkLoadingTile> void loadChunksForTile(@Nonnull T tile) {
        if (TICKETS.containsKey(tile)) {
            //safety check in case we already have one
            updateChunksFor(tile);
            return;
        }
        ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(BCLib.INSTANCE, tile.getWorld(), ForgeChunkManager.Type.NORMAL);
        if (ticket == null) {
            BCLog.logger.warn("Chunkloading failed, most likely the limit was reached");
            return;
        }

        if(!canLoadFor(tile)) {
            return;
        }

        ticket.getModData().setTag("location", NBTUtilBC.writeBlockPos(tile.getPos()));

        for(ChunkPos chunkPos: getChunksToLoad(tile)) {
            ForgeChunkManager.forceChunk(ticket, chunkPos);
        }
        TICKETS.put(tile, ticket);
    }

    public static void releaseChunksFor(@Nonnull IChunkLoadingTile tile) {
        ForgeChunkManager.releaseTicket(TICKETS.get(tile));
        TICKETS.remove(tile);
    }

    public static <T extends TileEntity & IChunkLoadingTile> void updateChunksFor(@Nonnull T tile) {
        if (!TICKETS.containsKey(tile))
            loadChunksForTile(tile);
        ForgeChunkManager.Ticket ticket = TICKETS.get(tile);
        Collection<ChunkPos> chunks = getChunksToLoad(tile);
        for (ChunkPos pos: ticket.getChunkList()) {
            if (!chunks.contains(pos)) {
                ForgeChunkManager.unforceChunk(ticket, pos);
            }
        }

        for (ChunkPos pos: chunks) {
            if (!ticket.getChunkList().contains(pos)) {
                ForgeChunkManager.forceChunk(ticket, pos);
            }
        }
    }

    private static <T extends TileEntity & IChunkLoadingTile> Collection<ChunkPos> getChunksToLoad(@Nonnull T tile) {
        Collection<ChunkPos> chunks = tile.getChunksToLoad();
        if (chunks == null) {
            chunks = new ArrayList<>();
        }
        ChunkPos pos = new ChunkPos(tile.getPos());
        if (!chunks.contains(pos)) {
            chunks.add(pos);
        }
        return chunks;
    }

    public static <T extends TileEntity & IChunkLoadingTile> void rebindTickets(List<ForgeChunkManager.Ticket> tickets, World world) {
        if (BCLibConfig.chunkLoadingLevel != BCLibConfig.ChunkLoaderLevel.NONE) {
            for (ForgeChunkManager.Ticket ticket : tickets) {
                TileEntity tile = world.getTileEntity(NBTUtilBC.readBlockPos(ticket.getModData().getTag("location")));
                if (tile == null || !(tile instanceof IChunkLoadingTile) || !canLoadFor((IChunkLoadingTile) tile))
                    continue;
                for(ChunkPos chunkPos: getChunksToLoad((T) tile)) {
                    ForgeChunkManager.forceChunk(ticket, chunkPos);
                }
                TICKETS.put((IChunkLoadingTile) tile, ticket);
            }
        }
    }

    private static boolean canLoadFor(@Nonnull IChunkLoadingTile tile) {
        return !(tile.getLoadType() == null || BCLibConfig.chunkLoadingLevel == BCLibConfig.ChunkLoaderLevel.NONE ||
            (BCLibConfig.chunkLoadingLevel == BCLibConfig.ChunkLoaderLevel.STRICT_TILES && tile.getLoadType() == IChunkLoadingTile.LoadType.SOFT));
    }
}