/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.player.EntityPlayer;

public interface IFastAddonRenderer<T extends Addon> {
    void renderAddonFast(T addon, EntityPlayer player, float partialTicks, VertexBuffer vb);

    default IFastAddonRenderer<T> then(IFastAddonRenderer<? super T> after) {
        return (addon, player, partialTicks, vb) -> {
            renderAddonFast(addon, player, partialTicks, vb);
            after.renderAddonFast(addon, player, partialTicks, vb);
        };
    }
}
