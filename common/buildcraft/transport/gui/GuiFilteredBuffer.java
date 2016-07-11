/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.transport.gui;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.transport.TransportSprites;
import buildcraft.transport.container.ContainerFilteredBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiFilteredBuffer extends GuiBC8<ContainerFilteredBuffer> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcrafttransport:textures/gui/filtered_buffer.png");
    private static final int SIZE_X = 176, SIZE_Y = 169;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiFilteredBuffer(ContainerFilteredBuffer container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        int x = guiLeft;
        int y = guiTop;
        ICON_GUI.drawAt(rootElement);
        for (int i = 0; i < 9; i++) {
            ItemStack stack = container.tile.invFilter.getStackInSlot(i);
            RenderHelper.enableGUIStandardItemLighting();
            int currentX = rootElement.getX() + 8 + i * 18;
            int currentY = rootElement.getY() + 61;
//            GL11.glEnable(GL11.GL_BLEND);
//            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            GL11.glColor4f(1, 1, 1, 0.5F);
            if(stack != null) {
                this.itemRender.renderItemAndEffectIntoGUI(this.mc.thePlayer, stack, currentX, currentY);
            } else {
                this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                this.drawTexturedModalRect(currentX, currentY, TransportSprites.NOTHING_FILTERED_BUFFER_SLOT.getSprite(), 16, 16);
            }
//            GL11.glColor4f(1, 1, 1, 1);
//            GL11.glDisable(GL11.GL_BLEND);
            RenderHelper.disableStandardItemLighting();
        }
//        GL11.glPushMatrix();
//        GL11.glTranslatef(0, 0, 100);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 0.7F);
        ICON_GUI.drawAt(rootElement);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
//        GL11.glPopMatrix();
    }

    @Override
    protected void drawForegroundLayer() {
        int x = guiLeft;
        int y = guiTop;
        String title = I18n.format("tile.filteredBufferBlock.name");
        int xPos = (xSize - fontRendererObj.getStringWidth(title)) / 2;
        fontRendererObj.drawString(title, x + xPos, y + 10, 0x404040);
    }
}
