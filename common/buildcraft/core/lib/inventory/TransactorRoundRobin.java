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

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;

public class TransactorRoundRobin extends TransactorSimple {

	public TransactorRoundRobin(IInventory inventory) {
		super(inventory);
	}

	@Override
	public int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd) {

		int added = 0;

		for (int itemLoop = 0; itemLoop < stack.stackSize; ++itemLoop) { // add 1 item n times.

			int smallestStackSize = Integer.MAX_VALUE;
			IInvSlot minSlot = null;

			for (IInvSlot slot : InventoryIterator.getIterable(inventory, orientation)) {
				ItemStack stackInInventory = slot.getStackInSlot();

				if (stackInInventory == null) {
					continue;
				}

				if (stackInInventory.stackSize >= stackInInventory.getMaxStackSize()) {
					continue;
				}

				if (stackInInventory.stackSize >= inventory.getInventoryStackLimit()) {
					continue;
				}

				if (StackHelper.canStacksMerge(stack, stackInInventory) && stackInInventory.stackSize < smallestStackSize) {
					smallestStackSize = stackInInventory.stackSize;
					minSlot = slot;
				}
				if (smallestStackSize <= 1) {
					break;
				}
			}

			if (minSlot != null) {
				added += addToSlot(minSlot, stack, stack.stackSize - 1, doAdd); // add 1 item n times, into the selected slot
			} else {
				break;
			}

		}

		return added;
	}
}
