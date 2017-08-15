/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;

import buildcraft.lib.client.sprite.DynamicTextureBC;
import buildcraft.lib.misc.RenderUtil;

public class GuideFont implements IFontRenderer {

    private final DynamicTextureBC tex = new DynamicTextureBC(512, 512);
    private final BufferedImage img;
    private final Graphics2D g2d;

    private final Font font;

    public GuideFont(Font font) {
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
    public int drawString(String text, int x, int y, int shade, float scale) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);
        double scaleFactor = mc.displayWidth / res.getScaledWidth_double();

        g2d.setColor(new Color(0, 0, 0, 255));
        g2d.fillRect(0, 0, 512, 512);
        g2d.setColor(new Color(0xFF_FF_FF));
        Font f2 = font.deriveFont(font.getSize2D() * scale * (float) scaleFactor);
        FontMetrics metrics = g2d.getFontMetrics(f2);
        g2d.setFont(f2);
        Rectangle2D rect = metrics.getStringBounds(text, g2d);
        // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // int font_height = (int) (getFontHeight() * scaleFactor);
        int font_height = (int) rect.getHeight();
        g2d.drawString(text, 0, font_height);

        for (int _x = 0; _x < 512; _x++) {
            for (int _y = 0; _y < font_height; _y++) {
                int rgb = img.getRGB(_x, _y);
                if ((rgb & 0xFF) == 0) rgb = 0;
                tex.setColor(_x, _y, rgb);
            }
        }
        GlStateManager.enableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.pushMatrix();
        GlStateManager.scale(1 / scaleFactor, 1 / scaleFactor, 1);
        if ((shade & 0xFF_00_00_00) == 0) {
            shade |= 0xFF_00_00_00;
        }
        RenderUtil.setGLColorFromIntPlusAlpha(shade);
        tex.draw((int) (x * scaleFactor), (int) (y * scaleFactor - metrics.getDescent()), 0);
        // tex.draw(x, y, 0);
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1);
        GlStateManager.enableDepth();

        return g2d.getFontMetrics().stringWidth(text);
    }
}
