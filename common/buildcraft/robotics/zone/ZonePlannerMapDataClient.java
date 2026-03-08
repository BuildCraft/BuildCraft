/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import buildcraft.lib.net.MessageManager;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ZonePlannerMapDataClient extends ZonePlannerMapData {
    public static final ZonePlannerMapDataClient INSTANCE = new ZonePlannerMapDataClient();

    private final List<ZonePlannerMapChunkKey> pending = new ArrayList<>();

    @Override
    public ZonePlannerMapChunk loadChunk(Level world, ZonePlannerMapChunkKey key) {
        if (!pending.contains(key)) {
            pending.add(key);
            MessageManager.sendToServer(new MessageZoneMapRequest(key));
        }
        return null;
    }

    public void onChunkReceived(ZonePlannerMapChunkKey key, ZonePlannerMapChunk zonePlannerMapChunk) {
        pending.remove(key);
        data.put(key, zonePlannerMapChunk);
    }
}
