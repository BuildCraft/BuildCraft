/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import net.minecraft.entity.player.PlayerEntity;

import buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;

public enum MarkerRenderer implements IDetachedRenderer {
    INSTANCE;

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void render(PlayerEntity player, float partialTicks) {
        for (MarkerCache<? extends MarkerSubCache<?>> cache : MarkerCache.CACHES) {
            for (MarkerConnection<?> connection : cache.getSubCache(player.getWorld()).getConnections()) {
                connection.renderInWorld();
            }
        }
    }
}
