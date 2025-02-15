/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.lib.client.render.HudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HudSingleSchematic extends HudRenderer {
    @Override
//    protected void renderImpl(Minecraft mc, EntityPlayerSP player)
    protected void renderImpl(Minecraft mc, LocalPlayer player) {

    }

    @Override
//    protected boolean shouldRender(Minecraft mc, EntityPlayerSP player)
    protected boolean shouldRender(Minecraft mc, LocalPlayer player) {
//        ItemStack stack = player.getHeldItemMainhand();
        ItemStack stack = player.getMainHandItem();
        return false;
    }
}
