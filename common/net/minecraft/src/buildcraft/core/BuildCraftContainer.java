/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.Container;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public abstract class BuildCraftContainer extends Container {

	public IInventory inventory;

	public BuildCraftContainer (IInventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public ItemStack transferStackInSlot(int i)
	{
        ItemStack itemstack = null;
        Slot slot = (Slot)inventorySlots.get(i);
        if(slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if(i < inventory.getSizeInventory())
            {
                if(!mergeItemStack(itemstack1, inventory.getSizeInventory(), inventorySlots.size(), true))
					return null;
            } else
            if(!mergeItemStack(itemstack1, 0, inventory.getSizeInventory(), false))
				return null;
            if(itemstack1.stackSize == 0)
				slot.putStack(null);
			else
				slot.onSlotChanged();
        }
        return itemstack;
	}

}
