/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partial) {
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
        int hoverState = getHoverState(mouseOver);
        drawTexturedModalRect(x, y, xOffset, yOffset + hoverState * h, width / 2, h);
        drawTexturedModalRect(x + width / 2, y, xOffset + w - width / 2, yOffset + hoverState * h, width / 2, h);
        mouseDragged(minecraft, mouseX, mouseY);
        drawCenteredString(fontrenderer, displayString, x + width / 2, y + (h - 8) / 2, getTextColor(mouseOver));
    }
}
