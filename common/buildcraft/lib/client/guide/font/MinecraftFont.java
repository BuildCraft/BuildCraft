/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.font;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

/** Implements a font that delegates to Minecraft's own {@link FontRenderer} */
public enum MinecraftFont implements IFontRenderer {
    INSTANCE;

    private static FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public int getStringWidth(String text) {
        return getFontRenderer().getStringWidth(text);
    }

    @Override
    public int getFontHeight(String text) {
        return getFontRenderer().FONT_HEIGHT;
    }

    @Override
    public int drawString(String text, int x, int y, int colour, float scale) {
        boolean _scale = scale != 1;
        if (_scale) {
            GlStateManager.pushMatrix();
            GL11.glScaled(scale, scale, 1);
            x = (int) (x / scale);
            y = (int) (y / scale);
        }
        int v = getFontRenderer().drawString(text, x, y, colour);
        v -= x;
        GlStateManager.color(1f, 1f, 1f);
        if (_scale) {
            GlStateManager.popMatrix();
            v = (int) (v * scale);
        }
        return v;
    }
}
