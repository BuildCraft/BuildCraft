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
        this(new RawSprite(texture, u, u, u + width, v + height, 256), 256);
    }

    public GuiIcon offset(int u, int v) {
        RawSprite raw = ((RawSprite) sprite);
        float uMin = raw.uMin + u / textureSize;
        float vMin = raw.vMin + v / textureSize;
        float uMax = raw.uMax + u / textureSize;
        float vMax = raw.vMax + v / textureSize;
        return new GuiIcon(new RawSprite(raw.location, uMin, vMin, uMax, vMax), textureSize);
    }

    public DynamicTexture createDynamicTexure(int scale) {
        return new DynamicTexture(width * scale, height * scale);
    }

    @Override
    public void drawAt(int x, int y) {
        this.drawScaledInside(x, y, this.width, this.height);
    }

    public void drawScaledInside(IPositionedElement element) {
        drawScaledInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawScaledInside(int x, int y, int drawnWidth, int drawnHeight) {
        draw(sprite, x, y, x + drawnWidth, y + drawnHeight);
    }

    public void drawCustomQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        sprite.bindTexture();

        double uMin = sprite.getInterpU(0);
        double uMax = sprite.getInterpU(1);

        double vMin = sprite.getInterpV(0);
        double vMax = sprite.getInterpV(1);

        Tessellator tess = Tessellator.getInstance();
        VertexBuffer vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        vertex(vb, x1, y1, uMin, vMax);
        vertex(vb, x2, y2, uMax, vMax);
        vertex(vb, x3, y3, uMax, vMin);
        vertex(vb, x4, y4, uMin, vMin);

        tess.draw();
    }

    public void drawCutInside(IPositionedElement element) {
        drawCutInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawCutInside(int x, int y, int displayWidth, int displayHeight) {
        // NEW
        sprite.bindTexture();

        displayWidth = Math.min(this.width, displayWidth);
        displayHeight = Math.min(this.height, displayHeight);

        int xMin = x;
        int yMin = y;

        int xMax = x + displayWidth;
        int yMax = y + displayHeight;

        double uMin = sprite.getInterpU(0);
        double vMin = sprite.getInterpV(0);

        double uMax = sprite.getInterpU(Math.min(1, displayWidth / (double) textureSize));
        double vMax = sprite.getInterpV(Math.min(1, displayHeight / (double) textureSize));

        Tessellator tess = Tessellator.getInstance();
        VertexBuffer vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        vertex(vb, xMin, yMax, uMin, vMax);
        vertex(vb, xMax, yMax, uMax, vMax);
        vertex(vb, xMax, yMin, uMax, vMin);
        vertex(vb, xMin, yMin, uMin, vMin);

        tess.draw();
    }

    public static void draw(ISprite sprite, int xMin, int yMin, int xMax, int yMax) {
        sprite.bindTexture();

        double uMin = sprite.getInterpU(0);
        double uMax = sprite.getInterpU(1);

        double vMin = sprite.getInterpV(0);
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
