/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class HudRenderer {
    // protected abstract void renderImpl(Minecraft mc, EntityPlayerSP player);
    protected abstract void renderImpl(Minecraft mc, LocalPlayer player);

    // protected abstract boolean shouldRender(Minecraft mc, EntityPlayerSP player);
    protected abstract boolean shouldRender(Minecraft mc, LocalPlayer player);

    protected void setupTransforms() {
    }

    public static void moveToHeldStack(Minecraft mc, int slot) {

    }

    // public final void render(Minecraft mc, EntityPlayerSP player)
    public final void render(Minecraft mc, PoseStack poseStack, LocalPlayer player) {
        if (shouldRender(mc, player)) {
//            GL11.glPushMatrix();
            poseStack.pushPose();
            setupTransforms();
            renderImpl(mc, player);
//            GL11.glPopMatrix();
            poseStack.popPose();
        }
    }
}
