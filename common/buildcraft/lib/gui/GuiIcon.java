package buildcraft.lib.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class GuiIcon implements ISimpleDrawable {
    public final ResourceLocation texture;
    public final int u, v, width, height;

    public GuiIcon(ResourceLocation texture, int u, int v, int width, int height) {
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }

    public GuiIcon(GuiIcon from, int uDiff, int vDiff) {
        this.texture = from.texture;
        this.u = from.u + uDiff;
        this.v = from.v + vDiff;
        this.width = from.width;
        this.height = from.height;
    }

    private void bindTexture() {
        if (texture != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        }
        GlStateManager.color(1, 1, 1);
    }

    public DynamicTexture createDynamicTexure(int scale) {
        return new DynamicTexture(width * scale, height * scale);
    }

    @Override
    public void drawAt(int x, int y) {
        bindTexture();
        int width = this.width;
        int height = this.height;
        int tileWidth = 256;
        int tileHeight = 256;
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, width, height, width, height, tileWidth, tileHeight);
    }

    public void drawScaledInside(IPositionedElement element) {
        drawScaledInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawScaledInside(int x, int y, int width, int height) {
        bindTexture();
        int uWidth = this.width;
        int vHeight = this.height;
        int tileWidth = 256;
        int tileHeight = 256;
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    public void drawCutInside(IPositionedElement element) {
        drawCutInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawCutInside(int x, int y, int width, int height) {
        bindTexture();
        width = Math.min(this.width, width);
        height = Math.min(this.height, height);
        int tileWidth = 256;
        int tileHeight = 256;
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, width, height, width, height, tileWidth, tileHeight);
    }
}
