/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import net.minecraft.server.network.ServerPlayerEntity;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
// STUB(R.Chen): // @SubscribeEvent — TODO(R.Chen): port to Fabric event removed — port to Fabric events
import net.minecraftforge.fml.common.gameevent.TickEvent;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.MessageManager;

import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public enum BCCoreEventDist {
    INSTANCE;

    // @SubscribeEvent — TODO(R.Chen): port to Fabric event
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.getWorld() != null && !event.getWorld().isClient && event.getWorld().getMinecraftServer() != null) {
            WorldSavedDataVolumeBoxes.get(event.getWorld()).tick();
        }
    }

    // @SubscribeEvent — TODO(R.Chen): port to Fabric event
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity) {
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayedServer(() ->
                MessageManager.sendTo(
                    new MessageVolumeBoxes(WorldSavedDataVolumeBoxes.get(event.getEntity().getWorld()).volumeBoxes),
                    (ServerPlayerEntity) event.getEntity()
                )
            );
            WorldSavedDataVolumeBoxes.get(((ServerPlayerEntity) event.getEntity()).getWorld()).volumeBoxes.stream()
                .filter(volumeBox -> volumeBox.isPausedEditingBy((ServerPlayerEntity) event.getEntity()))
                .forEach(VolumeBox::resumeEditing);
        }
    }
}
