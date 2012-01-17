/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.RefineryRecipe;
import net.minecraft.src.buildcraft.core.GuiAdvancedInterface;

public class GuiRefinery extends GuiAdvancedInterface {

	private TileRefinery refinery;
	
	public GuiRefinery(InventoryPlayer inventory, TileRefinery refinery) {
		super(new ContainerRefinery(inventory, refinery));
		
		xSize = 175;
		ySize = 207;
		
		this.refinery = refinery;
		
		this.slots = new AdvancedSlot [3];
		
		this.slots [0] = new ItemSlot(38, 54);
		this.slots [1] = new ItemSlot(126, 54);
		this.slots [2] = new ItemSlot(82, 54);	
		
		if (refinery.getFilter(0) != 0) {
			((ItemSlot) slots[0]).stack = new ItemStack(refinery.getFilter(0), 1,
					0);
		}
		
		if (refinery.getFilter(1) != 0) {
			((ItemSlot) slots[1]).stack = new ItemStack(refinery.getFilter(1), 1,
					0);
		}
		
		RefineryRecipe r = BuildCraftAPI.findRefineryRecipe(refinery.getFilter(0),
				BuildCraftAPI.BUCKET_VOLUME, refinery.getFilter(1), BuildCraftAPI.BUCKET_VOLUME);

		if (r != null) {
			((ItemSlot) slots[2]).stack = new ItemStack(
					r.resultId, 1, 0);
		}
	}


	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Refinery Setup", 28, 6, 0x404040);
		fontRenderer.drawString("->", 63, 59, 0x404040);
		fontRenderer.drawString("<-", 106, 59, 0x404040);
		fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
		
		drawForegroundSelection ();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/net/minecraft/src/buildcraft/factory/gui/refinery_filter.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		
		drawBackgroundSlots();
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		
		int position = getSlotAtLocation (i - cornerX, j - cornerY);
		
		AdvancedSlot slot = null;

		if (position != -1 && position != 2) {
			slot = slots[position];
		}
		
		if (slot != null) {
			int liquidId = BuildCraftAPI.getLiquidForFilledItem(mc.thePlayer.inventory
					.getItemStack());
			
			if (liquidId == 0) {
				((ItemSlot) slot).stack = null;
				
			} else {
				((ItemSlot) slot).stack = new ItemStack(
						liquidId, 1, 0);				
			}
			
			refinery.setFilter(position, liquidId);
			
			RefineryRecipe r = BuildCraftAPI.findRefineryRecipe(refinery.getFilter(0),
					BuildCraftAPI.BUCKET_VOLUME, refinery.getFilter(1), BuildCraftAPI.BUCKET_VOLUME);

			if (r != null) {
				((ItemSlot) slots[2]).stack = new ItemStack(
						r.resultId, 1, 0);
			} else {
				((ItemSlot) slots[2]).stack = null;
			}
		}
	}
	
}
