package ct.buildcraft.lib.gui;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.client.sprite.SpriteRaw;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.gui.pos.IGuiPosition;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class GuiIcon implements ISimpleDrawable{
    public final ISprite sprite;
    public final int textureSize;
    public final int width, height;

    public GuiIcon(ISprite sprite, int textureSize) {
        this.sprite = sprite;
        this.textureSize = textureSize;
        this.width = (int) (Math.abs(sprite.getInterpU(1) - sprite.getInterpU(0)) * textureSize);
        this.height = (int) (Math.abs(sprite.getInterpV(1) - sprite.getInterpV(0)) * textureSize);
    }

    public GuiIcon(ResourceLocation texture, double u, double v, double width, double height, int texSize) {
        this(new SpriteRaw(texture, u, v, width, height, texSize), texSize);
    }

    public GuiIcon(ResourceLocation texture, double u, double v, double width, double height) {
        this(texture, u, v, width, height, 256);
    }

    public GuiIcon offset(double u, double v) {
        SpriteRaw raw = (SpriteRaw) sprite;
        double uMin = raw.uMin + u / textureSize;
        double vMin = raw.vMin + v / textureSize;
        return new GuiIcon(new SpriteRaw(raw.location, uMin, vMin, raw.width, raw.height), textureSize);
    }

    public boolean containsGuiPos(double x, double y, IGuiPosition pos) {
        return new GuiRectangle(x, y, width, height).contains(pos);
    }

    public DynamicTexture createDynamicTexture(int scale) {
        return new DynamicTexture(width * scale, height * scale, false);
    }

    @Override
    public void drawAt(PoseStack pose, double x, double y) {
        this.drawScaledInside(pose, x, y, this.width, this.height);
    }

    public void drawScaledInside(PoseStack pose, IGuiArea element) {
        drawScaledInside(pose, element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawScaledInside(PoseStack pose, double x, double y, double drawnWidth, double drawnHeight) {
        draw(pose, sprite, x, y, x + drawnWidth, y + drawnHeight);
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

    private static double[] calcQ(double x1, double y1, double x2, double y2, double x3, double y3, double x4,
        double y4) {
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

    public void drawCutInside(PoseStack pose, IGuiArea element) {
        drawCutInside(pose, element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawCutInside(PoseStack pose, double x, double y, double displayWidth, double displayHeight) {
    	Matrix4f matrix4f = pose.last().pose();
        sprite.bindTexture();

        displayWidth = Math.min(this.width, displayWidth);
        displayHeight = Math.min(this.height, displayHeight);

        double xMin = x;
        double yMin = y;

        double xMax = x + displayWidth;
        double yMax = y + displayHeight;

        float uMin = sprite.getInterpU(0);
        float vMin = sprite.getInterpV(0);

        float uMax = sprite.getInterpU(displayWidth / width);
        float vMax = sprite.getInterpV(displayHeight / height);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder vb = tess.getBuilder();
        vb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        vertex(matrix4f, vb, xMin, yMax, uMin, vMax);
        vertex(matrix4f, vb, xMax, yMax, uMax, vMax);
        vertex(matrix4f, vb, xMax, yMin, uMax, vMin);
        vertex(matrix4f, vb, xMin, yMin, uMin, vMin);

        tess.end();
    }

    public static void drawAt(PoseStack pose, ISprite sprite, double x, double y, double size) {
        drawAt(pose, sprite, x, y, size, size);
    }

    public static void drawAt(PoseStack pose, ISprite sprite, double x, double y, double width, double height) {
        draw(pose, sprite, x, y, x + width, y + height);
    }

    public static void draw(PoseStack pose, ISprite sprite, double xMin, double yMin, double xMax, double yMax) {
    	Matrix4f matrix4f = pose.last().pose();
        sprite.bindTexture();

        float uMin = sprite.getInterpU(0);
        float vMin = sprite.getInterpV(0);

        float uMax = sprite.getInterpU(1);
        float vMax = sprite.getInterpV(1);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder vb = tess.getBuilder();
        vb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        vertex(matrix4f, vb, xMin, yMax, uMin, vMax);
        vertex(matrix4f, vb, xMax, yMax, uMax, vMax);
        vertex(matrix4f, vb, xMax, yMin, uMax, vMin);
        vertex(matrix4f, vb, xMin, yMin, uMin, vMin);

        tess.end();
    }

    private static void vertex(Matrix4f m, BufferBuilder vb, double x, double y, float u, float v) {
        vb.vertex(m, (float)x, (float)y, 0);
        vb.uv(u, v);
        vb.endVertex();
    }
}
