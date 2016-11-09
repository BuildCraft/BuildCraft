/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */

package buildcraft.lib.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.GuiBC8;

@SideOnly(Side.CLIENT)
public class GuiImageButton extends GuiAbstractButton {
    private final int size, u, v, baseU, baseV;
    private final ResourceLocation texture;

    public GuiImageButton(GuiBC8<?> gui, int id, int x, int y, int size, ResourceLocation texture, int u, int v) {
        this(gui, id, x, y, size, texture, 0, 0, u, v);
    }

    public GuiImageButton(GuiBC8<?> gui, int id, int x, int y, int size, ResourceLocation texture, int baseU, int baseV, int u, int v) {
        super(gui, id, x, y, size, size, "");
        this.size = size;
        this.u = u;
        this.v = v;
        this.baseU = baseU;
        this.baseV = baseV;
        this.texture = texture;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void drawButton(Minecraft minecraft, int x, int y) {
        if (!visible) {
            return;
        }

        minecraft.renderEngine.bindTexture(texture);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

        int buttonState = getButtonState();

        drawTexturedModalRect(xPosition, yPosition, baseU + buttonState * size, baseV, size, size);
        drawTexturedModalRect(xPosition + 1, yPosition + 1, u, v, size - 2, size - 2);

        mouseDragged(minecraft, x, y);
    }

    private int getButtonState() {
        if (!this.enabled) {
            return 0;
        }

        if (isMouseOver()) {
            if (!this.active) {
                return 2;
            } else {
                return 4;
            }
        }

        if (!this.active) {
            return 1;
        } else {
            return 3;
        }
    }
}
