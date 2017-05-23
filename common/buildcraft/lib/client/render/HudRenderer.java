/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public abstract class HudRenderer {
    protected abstract void renderImpl(Minecraft mc, EntityPlayerSP player);

    protected abstract boolean shouldRender(Minecraft mc, EntityPlayerSP player);

    protected void setupTransforms() {}

    public static void moveToHeldStack(Minecraft mc, int slot) {

    }

    public final void render(Minecraft mc, EntityPlayerSP player) {
        if (shouldRender(mc, player)) {
            GL11.glPushMatrix();
            setupTransforms();
            renderImpl(mc, player);
            GL11.glPopMatrix();
        }
    }
}
