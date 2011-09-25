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
	public final ItemStack transferStackInSlot(int i)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot)inventorySlots.get(i);
		if(slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (i < inventorySize) {
				mergeItemStack(itemstack1, inventorySize, inventorySlots.size(), true);
			} else {
				mergeItemStack(itemstack1, 0, inventorySize, false);
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
