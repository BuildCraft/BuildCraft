/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.gui.button;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.gui.GuiBC8;

@SideOnly(Side.CLIENT)
public class GuiBetterButton extends GuiAbstractButton {
    protected final IButtonTextureSet texture;

    public GuiBetterButton(GuiBC8<?> gui, int id, int x, int y, String label) {
        this(gui, id, x, y, 200, StandardButtonTextureSets.LARGE_BUTTON, label);
    }

    public GuiBetterButton(GuiBC8<?> gui, int id, int x, int y, int width, String label) {
        this(gui, id, x, y, width, StandardButtonTextureSets.LARGE_BUTTON, label);
    }

    public GuiBetterButton(GuiBC8<?> gui, int id, int x, int y, int width, IButtonTextureSet texture, String label) {
        super(gui, id, x, y, width, texture.getHeight(), label);
        this.texture = texture;
    }

    @Override
    public int getHeight() {
        return texture.getHeight();
    }

    public int getTextColor(boolean mouseOver) {
        if (!enabled) {
            return 0xffa0a0a0;
        } else if (mouseOver) {
            return 0xffffa0;
        } else {
            return 0xe0e0e0;
        }
    }

    protected void bindButtonTextures(Minecraft minecraft) {
        minecraft.renderEngine.bindTexture(texture.getTexture());
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

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }

        FontRenderer fontrenderer = minecraft.fontRenderer;
        bindButtonTextures(minecraft);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int xOffset = texture.getX();
        int yOffset = texture.getY();
        int h = texture.getHeight();
        int w = texture.getWidth();
        boolean mouseOver = isMouseOver();
        int buttonState = getButtonState();
        drawTexturedModalRect(xPosition, yPosition, xOffset, yOffset + buttonState * h, width / 2, h);
        drawTexturedModalRect(xPosition + width / 2, yPosition, xOffset + w - width / 2, yOffset + buttonState * h, width / 2, h);
        mouseDragged(minecraft, mouseX, mouseY);
        drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (h - 8) / 2, getTextColor(mouseOver));
    }
}
