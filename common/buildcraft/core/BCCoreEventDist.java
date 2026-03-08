/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.MessageManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public enum BCCoreEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
//        if (event.world != null && !event.world.isClientSide && event.world.getMinecraftServer() != null)
        if (event.world != null && !event.world.isClientSide && event.world.getServer() != null) {
            WorldSavedDataVolumeBoxes.get(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayedServer(() ->
                    MessageManager.sendTo(
                            new MessageVolumeBoxes(WorldSavedDataVolumeBoxes.get(event.getEntity().level).volumeBoxes),
                            serverPlayer
                    )
            );
            WorldSavedDataVolumeBoxes.get(serverPlayer.level).volumeBoxes.stream()
                    .filter(volumeBox -> volumeBox.isPausedEditingBy(serverPlayer))
                    .forEach(VolumeBox::resumeEditing);
        }
    }
}
