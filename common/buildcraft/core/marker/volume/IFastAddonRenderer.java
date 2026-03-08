/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.entity.player.PlayerEntity;

public interface IFastAddonRenderer<T extends Addon> {
    //    void renderAddonFast(T addon, PlayerEntity player, float partialTicks, BufferBuilder bb);
    void renderAddonFast(T addon, PlayerEntity player, Entry pose, float partialTicks, IVertexBuilder bb);

    default IFastAddonRenderer<T> then(IFastAddonRenderer<? super T> after) {
        return (addon, player, pose, partialTicks, bb) ->
        {
            renderAddonFast(addon, player, pose, partialTicks, bb);
            after.renderAddonFast(addon, player, pose, partialTicks, bb);
        };
    }
}
