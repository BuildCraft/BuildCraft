/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class BuildCraftContainer extends Container {

	private int inventorySize;
	
	public BuildCraftContainer (int inventorySize) {
		this.inventorySize = inventorySize;
	}
	
	@Override	
	public final ItemStack getStackInSlot(int i)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot)inventorySlots.get(i);
		if(slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (i < inventorySize) {
				func_28126_a(itemstack1, inventorySize, inventorySlots.size(), true);
			} else {
				func_28126_a(itemstack1, 0, inventorySize, false);
			}

			if(itemstack1.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

	@Override
	public final boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

}
