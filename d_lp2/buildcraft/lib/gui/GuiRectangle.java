package buildcraft.lib.gui;

import net.minecraft.client.renderer.texture.DynamicTexture;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiRectangle {
    public final int x, y, width, height;

    public GuiRectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isMouseInside(int mouseX, int mouseY) {
        return isMouseInside(x, y, mouseX, mouseY);
    }

    public boolean isMouseInside(int x, int y, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @SideOnly(Side.CLIENT)
    public DynamicTexture createDynamicTexure(int scale) {
        return new DynamicTexture(width * scale, height * scale);
    }

    @Override
    public String toString() {
        return "Rectangle [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }
}
