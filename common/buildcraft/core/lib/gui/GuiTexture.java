package buildcraft.core.lib.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class GuiTexture {
    public static class Rectangle {
        public final int x, y, width, height;

        public Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean isMouseInside(int x, int y, int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public DynamicTexture createDynamicTexure(int scale) {
            return new DynamicTexture(width * scale, height * scale);
        }

        @Override
        public String toString() {
            return "Rectangle [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
        }
    }

    public static class GuiIcon extends Rectangle {
        public final ResourceLocation texture;

        public GuiIcon(ResourceLocation texture, int u, int v, int width, int height) {
            super(u, v, width, height);
            this.texture = texture;
        }

        private void bindTexture() {
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
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
}
