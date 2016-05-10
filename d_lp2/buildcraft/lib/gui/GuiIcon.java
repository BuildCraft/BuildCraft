package buildcraft.lib.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiIcon extends GuiRectangle {
    public final ResourceLocation texture;

    public GuiIcon(ResourceLocation texture, int u, int v, int width, int height) {
        super(u, v, width, height);
        this.texture = texture;
    }

    private void bindTexture() {
        if (texture != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        }
        GlStateManager.color(1, 1, 1);
    }

    public void draw(IPositionedElement element) {
        draw(element.getX(), element.getY());
    }

    public void draw(int x, int y) {
        bindTexture();
        Gui.drawModalRectWithCustomSizedTexture(x, y, this.x, this.y, width, height, 256, 256);
    }

    public void drawScaled(int x, int y, int scaledWidth, int scaledHeight) {
        bindTexture();
        Gui.drawScaledCustomSizeModalRect(x, y, this.x, this.y, width, height, scaledWidth, scaledHeight, 256, 256);
    }

    public void drawScaledInside(GuiRectangle rectangle) {
        drawScaled(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public void drawCutInside(IPositionedElement element) {
        bindTexture();
        final int x2 = element.getX();
        final int y2 = element.getY();
        final int w2 = element.getWidth();
        final int h2 = element.getHeight();
        Gui.drawModalRectWithCustomSizedTexture(x2, y2, this.x, this.y, Math.min(width, w2), Math.min(height, h2), 256, 256);
    }
}
