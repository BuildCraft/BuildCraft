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

    public void draw(int x, int y) {
        bindTexture();
        Gui.drawModalRectWithCustomSizedTexture(x, y, this.x, this.y, width, height, 256, 256);
    }

    public void drawScaled(int x, int y, int scaledWidth, int scaledHeight) {
        bindTexture();
        Gui.drawScaledCustomSizeModalRect(x, y, this.x, this.y, width, height, scaledWidth, scaledHeight, 256, 256);
    }
}