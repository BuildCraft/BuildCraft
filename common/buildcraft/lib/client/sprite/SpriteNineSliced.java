/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.pos.IGuiArea;

/** Defines and draws a 9-sliced sprite. */
@SideOnly(Side.CLIENT)
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
        sprite.bindTexture();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.setTranslation(x, y, 0);

        double sx = xScale;
        double sy = yScale;
        double[] xa = { 0, xMin * sx, width + (xMax - 1) * sx, width };
        double[] ya = { 0, yMin * sy, height + (yMax - 1) * sy, height };

        double[] ua = { 0, xMin, xMax, 1 };
        double[] va = { 0, yMin, yMax, 1 };

        quad(vb, xa, ya, ua, va, 0, 0);
        quad(vb, xa, ya, ua, va, 0, 1);
        quad(vb, xa, ya, ua, va, 0, 2);

        quad(vb, xa, ya, ua, va, 1, 0);
        quad(vb, xa, ya, ua, va, 1, 1);
        quad(vb, xa, ya, ua, va, 1, 2);

        quad(vb, xa, ya, ua, va, 2, 0);
        quad(vb, xa, ya, ua, va, 2, 1);
        quad(vb, xa, ya, ua, va, 2, 2);

        tess.draw();
        vb.setTranslation(0, 0, 0);
    }

    private void quad(BufferBuilder vb, double[] x, double[] y, double[] u, double[] v, int xIndex, int yIndex) {
        int xis = xIndex;
        int xIB = xIndex + 1;

        int yis = yIndex;
        int yIB = yIndex + 1;

        vertex(vb, x[xis], y[yis], u[xis], v[yis]);
        vertex(vb, x[xis], y[yIB], u[xis], v[yIB]);
        vertex(vb, x[xIB], y[yIB], u[xIB], v[yIB]);
        vertex(vb, x[xIB], y[yis], u[xIB], v[yis]);
    }

    private void vertex(BufferBuilder vb, double x, double y, double texU, double texV) {
        vb.pos(x, y, 0);
        vb.tex(sprite.getInterpU(texU), sprite.getInterpV(texV));
        vb.endVertex();
    }
}
