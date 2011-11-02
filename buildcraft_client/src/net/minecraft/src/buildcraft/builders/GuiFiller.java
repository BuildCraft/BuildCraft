/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;

public class GuiFiller extends GuiContainer {
	
	IInventory playerInventory;
	TileFiller filler;
	
	public GuiFiller(IInventory playerInventory, TileFiller filler) {
		super(new CraftingFiller(playerInventory, filler));
		this.playerInventory = playerInventory;
		this.filler = filler;
		xSize = 175;
		ySize = 240;
	}
	
    protected void drawGuiContainerForegroundLayer() {        
        fontRenderer.drawString("Filler", 75, 6, 0x404040);
        fontRenderer.drawString("Filling Resources", 8, 74, 0x404040);
        fontRenderer.drawString(playerInventory.getInvName(), 8, 142, 0x404040);        
    }
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
		int i = mc.renderEngine
				.getTexture("/net/minecraft/src/buildcraft/builders/gui/filler.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		
		if (filler.currentPattern != null) {
			i = mc.renderEngine
			.getTexture(filler.currentPattern.getTextureFile());
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(i);
			j = (width - xSize) / 2;
			k = (height - ySize) / 2;

			int textureI = filler.currentPattern.getTextureIndex() >> 4;
			int textureJ = filler.currentPattern.getTextureIndex() - textureI * 16;			
			
			drawTexturedModalRect(j + 125, k + 34, 16 * textureJ, 16 * textureI, 16, 16);
		}
		
	}

	int inventoryRows = 6;
}
