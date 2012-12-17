/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import buildcraft.core.gui.BuildCraftContainer;

public class ContainerFiller extends BuildCraftContainer {

	IInventory playerIInventory;
	IInventory fillerInventory;

	public ContainerFiller(IInventory playerInventory, IInventory fillerInventory) {
		super(fillerInventory.getSizeInventory());
		this.playerIInventory = playerInventory;
		this.fillerInventory = fillerInventory;

		for (int k = 0; k < 3; k++) {
			for (int j1 = 0; j1 < 3; j1++) {
				addSlotToContainer(new Slot(fillerInventory, j1 + k * 3, 31 + j1 * 18, 16 + k * 18));
			}
		}

		for (int k = 0; k < 3; k++) {
			for (int j1 = 0; j1 < 9; j1++) {
				addSlotToContainer(new Slot(fillerInventory, 9 + j1 + k * 9, 8 + j1 * 18, 85 + k * 18));
			}
		}

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 153 + l * 18));
			}

		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 211));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return fillerInventory.isUseableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer pl, int i) {
		ItemStack itemstack = null;
		Slot slot = (Slot) inventorySlots.get(i);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (i < getInventorySize()) {
				if (!mergeItemStack(itemstack1, getInventorySize(), inventorySlots.size(), true))
					return null;
			} else if (!mergeItemStack(itemstack1, 9, getInventorySize(), false))
				return null;
			if (itemstack1.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

}
