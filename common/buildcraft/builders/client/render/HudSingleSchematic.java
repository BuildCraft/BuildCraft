/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

import buildcraft.lib.client.render.HudRenderer;

public class HudSingleSchematic extends HudRenderer {
    @Override
    protected void renderImpl(Minecraft mc, EntityPlayerSP player) {

    }

    @Override
    protected boolean shouldRender(Minecraft mc, EntityPlayerSP player) {
        ItemStack stack = player.getHeldItemMainhand();
        return false;
    }
}
