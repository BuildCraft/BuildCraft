/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import ct.buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import ct.buildcraft.lib.marker.MarkerCache;
import ct.buildcraft.lib.marker.MarkerConnection;
import ct.buildcraft.lib.marker.MarkerSubCache;
import net.minecraft.world.entity.player.Player;

public enum MarkerRenderer implements IDetachedRenderer {
    INSTANCE;

    @Override
	public void render(PoseStack pose, Matrix4f matrix, Player player, float partialTicks) {
        for (MarkerCache<? extends MarkerSubCache<?>> cache : MarkerCache.CACHES) {
            for (MarkerConnection<?> connection : cache.getSubCache(player.level).getConnections()) {
                connection.renderInWorld(pose, matrix);
            }
        }
    }
}
