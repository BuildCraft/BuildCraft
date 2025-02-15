/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.transport.net.PipeItemMessageQueue;
import buildcraft.transport.wire.WorldSavedDataWireSystems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public enum BCTransportEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
//        if (!event.world.isRemote && event.world.getMinecraftServer() != null)
        if (!event.level.isClientSide && event.level.getServer() != null) {
            WorldSavedDataWireSystems.get(event.level).tick();
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        PipeItemMessageQueue.serverTick();
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent event) {
        WorldSavedDataWireSystems.get(event.getPlayer().level()).changedPlayers.add(event.getPlayer());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Post event) {
        // 1.18.2: no longer use GlList
//        PipeWireRenderer.clearWireCache();
    }

    @SubscribeEvent
//    public void onBlockPlace(BlockEvent.PlaceEvent event)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        // event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        // event.setCanceled(true);
    }
}
