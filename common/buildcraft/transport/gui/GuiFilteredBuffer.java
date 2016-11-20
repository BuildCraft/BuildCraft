/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.transport.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.container.ContainerFilteredBuffer;

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
        ICON_GUI.drawAt(rootElement);
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = container.tile.invFilter.getStackInSlot(i);
            int currentX = rootElement.getX() + 8 + i * 18;
            int currentY = rootElement.getY() + 61;
            // GL11.glEnable(GL11.GL_BLEND);
            // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            // GL11.glColor4f(1, 1, 1, 0.5F);
            if (stack != null) {
                this.itemRender.renderItemAndEffectIntoGUI(this.mc.thePlayer, stack, currentX, currentY);
            } else {
                this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                this.drawTexturedModalRect(currentX, currentY, BCTransportSprites.NOTHING_FILTERED_BUFFER_SLOT.getSprite(), 16, 16);
            }
            // GL11.glColor4f(1, 1, 1, 1);
            // GL11.glDisable(GL11.GL_BLEND);
        }
        RenderHelper.disableStandardItemLighting();
        // GL11.glPushMatrix();
        // GL11.glTranslatef(0, 0, 100);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, 0.7f);
        ICON_GUI.drawAt(rootElement);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        // GL11.glPopMatrix();
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
