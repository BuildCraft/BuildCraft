/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import buildcraft.core.lib.utils.Utils;

public class InventoryWrapperSimple extends InventoryWrapper {

	private final int[] slots;

	public InventoryWrapperSimple(IInventory inventory) {
		super(inventory);
		slots = Utils.createSlotArray(0, inventory.getSizeInventory());
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return slots;
	}

	@Override
	public boolean canInsertItem(int slotIndex, ItemStack itemstack, int side) {
		return isItemValidForSlot(slotIndex, itemstack);
	}

	@Override
	public boolean canExtractItem(int slotIndex, ItemStack itemstack, int side) {
		return true;
	}

}
