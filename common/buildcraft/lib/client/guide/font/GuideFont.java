/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.font;

import buildcraft.lib.client.sprite.DynamicTextureBC;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.RenderUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public class GuideFont implements IFontRenderer {

    private final DynamicTextureBC tex;
    private final BufferedImage img;
    private final Graphics2D g2d;

    private final Font font;

    public GuideFont(Font font) {
        this.tex = new DynamicTextureBC(512, 512, font.getFontName());
        this.font = font;
        this.img = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        this.g2d = img.createGraphics();
    }

    @Override
    public int getStringWidth(String text) {
        FontMetrics metrics = g2d.getFontMetrics(font);
        Rectangle2D rect = metrics.getStringBounds(text, g2d);
        return (int) rect.getWidth();
    }

    @Override
    public int getFontHeight(String text) {
        FontMetrics metrics = g2d.getFontMetrics(font);
        Rectangle2D rect = metrics.getStringBounds(text, g2d);
        return (int) rect.getHeight();
    }

    @Override
    public int getMaxFontHeight() {
        return g2d.getFontMetrics().getMaxAscent() + g2d.getFontMetrics().getMaxDescent();
    }

    @Override
    public int drawString(GuiGraphics guiGraphics, String text, int x, int y, int shade, boolean shadow, boolean centered, float scale) {
        PoseStack poseStack = guiGraphics.pose();
        text = ColourUtil.stripAllFormatCodes(text);
        Minecraft mc = Minecraft.getInstance();
//        ScaledResolution res = new ScaledResolution(mc);
//        double scaleFactor = mc.getWindow().getWidth() / res.getScaledWidth_double();
        double scaleFactor = mc.getWindow().getGuiScale();

        g2d.setColor(new Color(0, 0, 0, 255));
        g2d.fillRect(0, 0, 512, 512);
        g2d.setColor(new Color(0xFF_FF_FF));
        Font f2 = font.deriveFont(font.getSize2D() * scale * (float) scaleFactor);
        if (shadow) {
            f2 = f2.deriveFont(Font.BOLD);
        }
        FontMetrics metrics = g2d.getFontMetrics(f2);
        g2d.setFont(f2);
        Rectangle2D rect = metrics.getStringBounds(text, g2d);
        // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); //BC原来就是注释掉的
        // int font_height = (int) (getFontHeight() * scaleFactor);
        int font_height = metrics.getMaxAscent() + metrics.getMaxDescent();
        g2d.drawString(text, 0, metrics.getMaxAscent());

        for (int _x = 0; _x < 512; _x++) {
            for (int _y = 0; _y <= font_height; _y++) {
                int rgb = img.getRGB(_x, _y);
                if ((rgb & 0xFF) == 0) rgb = 0;
                tex.setColor(_x, _y, rgb);
            }
        }
//        GlStateManager.enableAlpha();
        RenderUtil.enableAlpha();
//        GlStateManager.disableDepth();
        RenderUtil.disableDepth();
//        GlStateManager.enableBlend();
        RenderUtil.enableBlend();
//        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//        GlStateManager.pushMatrix();
        poseStack.pushPose();
//        GlStateManager.scale(1 / scaleFactor, 1 / scaleFactor, 1);
        poseStack.scale((float) (1 / scaleFactor), (float) (1 / scaleFactor), 1);
        if ((shade & 0xFF_00_00_00) == 0) {
            shade |= 0xFF_00_00_00;
        }
        RenderUtil.setGLColorFromIntPlusAlpha(shade);
        if (centered) {
            x -= rect.getWidth() / 2 / scaleFactor;
        }
        tex.draw((int) (x * scaleFactor), (int) (y * scaleFactor - metrics.getMaxDescent()), 0, 0, 0,
                (int) (rect.getWidth()), (int) (rect.getHeight() + 1));
        // tex.draw(x, y, 0);
//        GlStateManager.popMatrix();
        poseStack.popPose();
//        GlStateManager.color(1, 1, 1);
        RenderUtil.color(1, 1, 1);
//        GlStateManager.enableDepth();
        RenderSystem.enableDepthTest();

        return (int) rect.getWidth();
    }

    @Override
    public List<String> wrapString(String text, int maxWidth, boolean shadow, float scale) {
        FontState state = new FontState(this, scale, shadow);

        return Collections.singletonList(text);
        // TODO Auto-generated method stub
        // throw new AbstractMethodError("// TODO: Implement this!");
    }

    private static class FontState {
        /**
         * <ol start="0">
         * <li>PLAIN</li>
         * <li>BOLD</li>
         * <li>ITALIC</li>
         * <li>BOLD ITALIC</li>
         * </ol>
         */
        final FontMetrics[] metrics = new FontMetrics[4];
        final boolean defaultShadow;

        FontState(GuideFont font, float scale, boolean shadow) {
            this.defaultShadow = shadow;
            Minecraft mc = Minecraft.getInstance();
//            ScaledResolution res = new ScaledResolution(mc);
//            double scaleFactor = mc.getWindow().getGuiScaledWidth() / res.getScaledWidth_double();
            double scaleFactor = mc.getWindow().getGuiScale();

            Font f2 = font.font.deriveFont(font.font.getSize2D() * scale * (float) scaleFactor);

            for (int i : new int[] { 0, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC }) {
                metrics[i] = font.g2d.getFontMetrics(f2.deriveFont(i));
            }
        }

        public int getPixelWidth(String text) {
            return 0;
        }
    }
}
