/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.core.marker.volume;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

// Yarn 1.20.1: net.minecraft.client.renderer.BufferBuilder → net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.BufferBuilder;
// Yarn 1.20.1: PlayerEntity → PlayerEntity
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public interface IFastAddonRenderer<T extends Addon> {
    void renderAddonFast(T addon, PlayerEntity player, float partialTicks, BufferBuilder bb);

    default IFastAddonRenderer<T> then(IFastAddonRenderer<? super T> after) {
        return (addon, player, partialTicks, bb) -> {
            renderAddonFast(addon, player, partialTicks, bb);
            after.renderAddonFast(addon, player, partialTicks, bb);
        };
    }
}
