/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.gui.pos.IGuiArea;

/** Defines and draws a 9-sliced sprite. */
public class SpriteNineSliced {
    public final ISprite sprite;
    public final double xMin, yMin, xMax, yMax;
    public final double scale;

    public SpriteNineSliced(ISprite sprite, int xMin, int yMin, int xMax, int yMax, int textureSize) {
        this.sprite = sprite;
        this.xMin = xMin / (double) textureSize;
        this.yMin = yMin / (double) textureSize;
        this.xMax = xMax / (double) textureSize;
        this.yMax = yMax / (double) textureSize;
        this.scale = textureSize;
    }

    public SpriteNineSliced(ISprite sprite, double xMin, double yMin, double xMax, double yMax, double scale) {
        this.sprite = sprite;
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.scale = scale;
    }

    public void draw(IGuiArea element) {
        draw(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void draw(double x, double y, double width, double height) {
        sprite.bindTexture();
        Tessellator tess = Tessellator.getInstance();
        VertexBuffer vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.setTranslation(x, y, 0);

        double sm = scale;
        double[] xa = { 0, xMin * sm, width + (xMax - 1) * sm, width };
        double[] ya = { 0, yMin * sm, height + (yMax - 1) * sm, height };

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

    private void quad(VertexBuffer vb, double[] x, double[] y, double[] u, double[] v, int xIndex, int yIndex) {
        int xis = xIndex;
        int xIB = xIndex + 1;

        int yis = yIndex;
        int yIB = yIndex + 1;

        vertex(vb, x[xis], y[yis], u[xis], v[yis]);
        vertex(vb, x[xis], y[yIB], u[xis], v[yIB]);
        vertex(vb, x[xIB], y[yIB], u[xIB], v[yIB]);
        vertex(vb, x[xIB], y[yis], u[xIB], v[yis]);
    }

    private void vertex(VertexBuffer vb, double x, double y, double texU, double texV) {
        vb.pos(x, y, 0);
        vb.tex(sprite.getInterpU(texU), sprite.getInterpV(texV));
        vb.endVertex();
    }
}
