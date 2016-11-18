package buildcraft.lib.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.sprite.ISprite;
import buildcraft.lib.client.sprite.RawSprite;
import buildcraft.lib.gui.pos.IPositionedElement;

public class GuiIcon implements ISimpleDrawable {
    public final ISprite sprite;
    public final int textureSize;
    public final int width, height;

    public GuiIcon(ISprite sprite, int textureSize) {
        this.sprite = sprite;
        this.textureSize = textureSize;
        this.width = (int) (Math.abs(sprite.getInterpU(1) - sprite.getInterpU(0)) * textureSize);
        this.height = (int) (Math.abs(sprite.getInterpV(1) - sprite.getInterpV(0)) * textureSize);
    }

    public GuiIcon(ResourceLocation texture, int u, int v, int width, int height) {
        this(new RawSprite(texture, u, v, width, height, 256), 256);
    }

    public GuiIcon offset(int u, int v) {
        RawSprite raw = (RawSprite) sprite;
        float uMin = raw.uMin + u / (float) textureSize;
        float vMin = raw.vMin + v / (float) textureSize;
        return new GuiIcon(new RawSprite(raw.location, uMin, vMin, raw.width, raw.height), textureSize);
    }

    public DynamicTexture createDynamicTexure(int scale) {
        return new DynamicTexture(width * scale, height * scale);
    }

    @Override
    public void drawAt(int x, int y) {
        this.drawScaledInside(x, y, this.width, this.height);
    }

    public void drawAt(double x, double y) {
        this.drawScaledInside(x, y, this.width, this.height);
    }

    public void drawScaledInside(IPositionedElement element) {
        drawScaledInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawScaledInside(double x, double y, double drawnWidth, double drawnHeight) {
        draw(sprite, x, y, x + drawnWidth, y + drawnHeight);
    }

    public void drawCustomQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        sprite.bindTexture();

        double uMin = sprite.getInterpU(0);
        double uMax = sprite.getInterpU(1);

        double vMin = sprite.getInterpV(0);
        double vMax = sprite.getInterpV(1);

        // Unfortunately we cannot use the vertex buffer directly (as it doesn't allow for texture4f)
        GL11.glBegin(GL11.GL_QUADS);

        double[] q = calcQ(x1, y1, x2, y2, x3, y3, x4, y4);

        vertDirect(x1, y1, uMin * q[0], vMax * q[0], 0, q[0]);
        vertDirect(x2, y2, uMax * q[1], vMax * q[1], 0, q[1]);
        vertDirect(x3, y3, uMax * q[2], vMin * q[2], 0, q[2]);
        vertDirect(x4, y4, uMin * q[3], vMin * q[3], 0, q[3]);

        GL11.glEnd();
    }

    private static double[] calcQ(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        // Method contents taken from http://www.bitlush.com/posts/arbitrary-quadrilaterals-in-opengl-es-2-0
        // (or github https://github.com/bitlush/android-arbitrary-quadrilaterals-in-opengl-es-2-0 if the site is down)
        // this code is by Keith Wood

        double ax = x3 - x1;
        double ay = y3 - y1;
        double bx = x4 - x2;
        double by = y4 - y2;

        double cross = ax * by - ay * bx;

        if (cross != 0) {
            double cy = y1 - y2;
            double cx = x1 - x2;

            double s = (ax * cy - ay * cx) / cross;

            if (s > 0 && s < 1) {
                double t = (bx * cy - by * cx) / cross;

                if (t > 0 && t < 1) {
                    double q0 = 1 / (1 - t);
                    double q1 = 1 / (1 - s);
                    double q2 = 1 / t;
                    double q3 = 1 / s;
                    return new double[] { q0, q1, q2, q3 };
                }
            }
        }
        // in case (for some reason) some of the input was wrong then we will fail back to default rendering
        return new double[] { 1, 1, 1, 1 };
    }

    private static void vertDirect(double x, double y, double s, double t, double r, double q) {
        GL11.glTexCoord4d(s, t, r, q);
        GL11.glVertex2d(x, y);
    }

    public void drawCutInside(IPositionedElement element) {
        drawCutInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawCutInside(double x, double y, double displayWidth, double displayHeight) {
        sprite.bindTexture();

        displayWidth = Math.min(this.width, displayWidth);
        displayHeight = Math.min(this.height, displayHeight);

        double xMin = x;
        double yMin = y;

        double xMax = x + displayWidth;
        double yMax = y + displayHeight;

        double uMin = sprite.getInterpU(0);
        double vMin = sprite.getInterpV(0);

        double uMax = sprite.getInterpU(displayWidth / width);
        double vMax = sprite.getInterpV(displayHeight / height);

        Tessellator tess = Tessellator.getInstance();
        VertexBuffer vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        vertex(vb, xMin, yMax, uMin, vMax);
        vertex(vb, xMax, yMax, uMax, vMax);
        vertex(vb, xMax, yMin, uMax, vMin);
        vertex(vb, xMin, yMin, uMin, vMin);

        tess.draw();
    }

    public static void draw(ISprite sprite, double xMin, double yMin, double xMax, double yMax) {
        sprite.bindTexture();

        double uMin = sprite.getInterpU(0);
        double vMin = sprite.getInterpV(0);

        double uMax = sprite.getInterpU(1);
        double vMax = sprite.getInterpV(1);

        Tessellator tess = Tessellator.getInstance();
        VertexBuffer vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        vertex(vb, xMin, yMax, uMin, vMax);
        vertex(vb, xMax, yMax, uMax, vMax);
        vertex(vb, xMax, yMin, uMax, vMin);
        vertex(vb, xMin, yMin, uMin, vMin);

        tess.draw();
    }

    private static void vertex(VertexBuffer vb, double x, double y, double u, double v) {
        vb.pos(x, y, 0);
        vb.tex(u, v);
        vb.endVertex();
    }
}
