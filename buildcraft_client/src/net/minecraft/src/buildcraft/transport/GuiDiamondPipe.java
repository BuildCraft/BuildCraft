/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.core.GuiAdvancedInterface;

public class GuiDiamondPipe extends GuiAdvancedInterface {
	
	IInventory playerInventory;
	IInventory filterInventory;
	
	public GuiDiamondPipe(IInventory playerInventory, IInventory filterInventory) {
		super(new CraftingDiamondPipe(playerInventory, filterInventory));
		this.playerInventory = playerInventory;
		this.filterInventory = filterInventory;
		xSize = 175;
		ySize = 225;
		
		slots = new AdvancedSlot [54];
		
		for(int k = 0; k < 6; k++)
        {
            for(int j1 = 0; j1 < 9; j1++)
            {
            	int id = k * 9 + j1;
                slots [id] = new ItemSlot(8 + j1 * 18, 18 + k * 18);
                ItemStack stack = filterInventory.getStackInSlot(
						j1 + k * 9);
                
                if (stack != null) {
                	((ItemSlot) slots[id]).stack = stack.copy();
                }
            }
        }
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString(filterInventory.getInvName(), 8, 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 97,
				0x404040);
		
		drawForegroundSelection ();
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine
				.getTexture("/net/minecraft/src/buildcraft/transport/gui/filter.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		
		drawBackgroundSlots();
	}

	int inventoryRows = 6;
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		
		int cornerX = (width - xSize) / 2;
		int cornerY = (height - ySize) / 2;
		
		int position = getSlotAtLocation (i - cornerX, j - cornerY);
		
		ItemSlot slot = null;

		if (position != -1) {
			slot = (ItemSlot) slots[position];
		}
		
		if (slot != null) {
			ItemStack playerStack = mc.thePlayer.inventory.getItemStack();
			
			if (playerStack != null) {
				ItemStack newStack = new ItemStack(playerStack.itemID, 1,
						playerStack.getItemDamage()); 
				slot.stack = newStack;
				filterInventory.setInventorySlotContents(position, newStack);
			} else {
				slot.stack = null;
				filterInventory.setInventorySlotContents(position, null);
			}
		}
	}
}
