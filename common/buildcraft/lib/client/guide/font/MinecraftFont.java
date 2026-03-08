/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.font;

import buildcraft.lib.misc.FontUtil;
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.List;

/** Implements a font that delegates to Minecraft's own {@link FontRenderer} */
public enum MinecraftFont implements IFontRenderer {
    INSTANCE;

    private static FontRenderer getFontRenderer() {
        return Minecraft.getInstance().font;
    }

    @Override
    public int getStringWidth(String text) {
        return getFontRenderer().width(text);
    }

    @Override
    public int getFontHeight(String text) {
        return getMaxFontHeight();
    }

    @Override
    public int getMaxFontHeight() {
        return getFontRenderer().lineHeight;
    }

    @Override
    public int drawString(MatrixStack poseStack, String text, int x, int y, int colour, boolean shadow, boolean centered, float scale) {
        boolean _scale = scale != 1;
        if (_scale) {
//            GlStateManager.pushMatrix();
            poseStack.pushPose();
//            GL11.glScaled(scale, scale, 1);
            poseStack.scale(scale, scale, 1);
            x = (int) (x / scale);
            y = (int) (y / scale);
        }
        if (centered) {
            x -= getStringWidth(text) / 2;
        }

//        int v = getFontRenderer().drawString(text, x, y, colour, shadow);
        int v;
        if (shadow) {
            v = getFontRenderer().drawShadow(poseStack, text, x, y, colour);
        } else {
            v = getFontRenderer().draw(poseStack, text, x, y, colour);
        }
        v -= x;
//        GlStateManager.color(1f, 1f, 1f);
        RenderUtil.color(1f, 1f, 1f);
        if (_scale) {
//            GlStateManager.popMatrix();
            poseStack.popPose();
            v = (int) (v * scale);
        }
        return v;
    }

    @Override
    public List<String> wrapString(String text, int maxWidth, boolean shadow, float scale) {
//        return getFontRenderer().listFormattedStringToWidth(text, (int) (maxWidth / scale));
        return FontUtil.listFormattedStringToWidth(text, (int) (maxWidth / scale));
    }
}
