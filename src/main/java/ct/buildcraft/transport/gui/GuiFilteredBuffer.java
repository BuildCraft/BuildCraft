/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.transport.gui;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFilteredBuffer extends GuiBC8<ContainerFilteredBuffer_BC8> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcrafttransport:textures/gui/filtered_buffer.png");
    private static final int SIZE_X = 176, SIZE_Y = 169;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiFilteredBuffer(ContainerFilteredBuffer_BC8 container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);
        //RenderHelper.enableGUIStandardItemLighting();
/*        for (int i = 0; i < 9; i++) {
            ItemStack stack = container.tile.invFilter.getStackInSlot(i);
            double currentX = mainGui.rootElement.getX() + 8 + i * 18;
            double currentY = mainGui.rootElement.getY() + 61;
            // GL11.glEnable(GL11.GL_BLEND);
            // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            // GL11.glColor4f(1, 1, 1, 0.5F);
            if (!stack.isEmpty()) {
                this.itemRenderer.renderAndDecorateItem(stack, (int) currentX, (int) currentY);
            } else {
                this.minecraft.getTextureManager().bindForSetup(InventoryMenu.BLOCK_ATLAS);
                this.drawTexturedModalRect((int) currentX, (int) currentY, BCTransportSprites.NOTHING_FILTERED_BUFFER_SLOT.getSprite(), 16, 16);
            }
            // GL11.glColor4f(1, 1, 1, 1);
            // GL11.glDisable(GL11.GL_BLEND);
        }*/
        //RenderHelper.disableStandardItemLighting();
        // GL11.glPushMatrix();
        // GL11.glTranslatef(0, 0, 100);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.clearColor(1, 1, 1, 0.7f);
        ICON_GUI.drawAt(pose, mainGui.rootElement);

        RenderSystem.clearColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        // GL11.glPopMatrix();
    }

    @Override
    protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        Component title = Component.translatable("tile.filteredBufferBlock.name");
        int xPos = (imageWidth - font.width(title)) / 2;
        font.draw(pose, title, x + xPos, y + 10, 0x404040);
    }
}
