/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.client.sprite;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import com.mojang.blaze3d.systems.RenderSystem;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.pos.IGuiArea;

/** Defines and draws a 9-sliced sprite from the block atlas. */
@Environment(EnvType.CLIENT)
public class SpriteNineSliced {
    public final ISprite sprite;
    public final double xMin, yMin, xMax, yMax;
    public final double xScale, yScale;

    public SpriteNineSliced(ISprite sprite, int xMin, int yMin, int xMax, int yMax, int textureSize) {
        this(sprite, xMin, yMin, xMax, yMax, textureSize, textureSize);
    }

    public SpriteNineSliced(ISprite sprite, int xMin, int yMin, int xMax, int yMax, int xScale, int yScale) {
        this.sprite = sprite;
        this.xMin = xMin / (double) xScale;
        this.yMin = yMin / (double) yScale;
        this.xMax = xMax / (double) xScale;
        this.yMax = yMax / (double) yScale;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public SpriteNineSliced(ISprite sprite, double xMin, double yMin, double xMax, double yMax, double scale) {
        this(sprite, xMin, yMin, xMax, yMax, scale, scale);
    }

    public SpriteNineSliced(ISprite sprite, double xMin, double yMin, double xMax, double yMax, double xScale,
        double yScale) {
        this.sprite = sprite;
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public void draw(IGuiArea element) {
        draw(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void draw(double x, double y, double width, double height) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        double sx = xScale, sy = yScale;
        double[] xa = { x, x + xMin * sx, x + width + (xMax - 1) * sx, x + width };
        double[] ya = { y, y + yMin * sy, y + height + (yMax - 1) * sy, y + height };
        double[] ua = { 0, xMin, xMax, 1 };
        double[] va = { 0, yMin, yMax, 1 };

        for (int xi = 0; xi < 3; xi++) {
            for (int yi = 0; yi < 3; yi++) {
                quad(buf, xa, ya, ua, va, xi, yi);
            }
        }

        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    private void quad(BufferBuilder buf, double[] x, double[] y, double[] u, double[] v, int xi, int yi) {
        vertex(buf, x[xi],   y[yi],   u[xi],   v[yi]);
        vertex(buf, x[xi],   y[yi+1], u[xi],   v[yi+1]);
        vertex(buf, x[xi+1], y[yi+1], u[xi+1], v[yi+1]);
        vertex(buf, x[xi+1], y[yi],   u[xi+1], v[yi]);
    }

    private void vertex(BufferBuilder buf, double x, double y, double texU, double texV) {
        buf.vertex((float) x, (float) y, 0)
            .texture((float) sprite.getInterpU(texU), (float) sprite.getInterpV(texV))
            .next();
    }
}
