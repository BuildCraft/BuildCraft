/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;

import org.lwjgl.opengl.GL11;

public class GuiBuilder extends GuiContainer {
	
	IInventory playerInventory;
	IInventory builderInventory;
	
	public GuiBuilder(IInventory playerInventory, IInventory filterInventory) {
		super(new CraftingBuilder(playerInventory, filterInventory));
		this.playerInventory = playerInventory;
		this.builderInventory = filterInventory;
		xSize = 175;
		ySize = 225;
	}
	
    protected void drawGuiContainerForegroundLayer() {        
        fontRenderer.drawString("Template", 67, 15, 0x404040);
        fontRenderer.drawString("Building Resources", 8, 60, 0x404040);
        fontRenderer.drawString("Inventory", 8, ySize - 97, 0x404040);
    }
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine
				.getTexture("/net/minecraft/src/buildcraft/builders/gui/builder.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	int inventoryRows = 6;
}
