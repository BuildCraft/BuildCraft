/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import java.util.Iterator;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

class InventoryIteratorSimple implements Iterable<IInvSlot> {

	private final IInventory inv;

	InventoryIteratorSimple(IInventory inv) {
		this.inv = InvUtils.getInventory(inv);
	}

	@Override
	public Iterator<IInvSlot> iterator() {
		return new Iterator<IInvSlot>() {
			int slot = 0;

			@Override
			public boolean hasNext() {
				return slot < inv.getSizeInventory();
			}

			@Override
			public IInvSlot next() {
				return new InvSlot(slot++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Remove not supported.");
			}
		};
	}

	private class InvSlot implements IInvSlot {

		private int slot;

		public InvSlot(int slot) {
			this.slot = slot;
		}

		@Override
		public ItemStack getStackInSlot() {
			return inv.getStackInSlot(slot);
		}

		@Override
		public void setStackInSlot(ItemStack stack) {
			inv.setInventorySlotContents(slot, stack);
		}

		@Override
		public boolean canPutStackInSlot(ItemStack stack) {
			return inv.isItemValidForSlot(slot, stack);
		}

		@Override
		public boolean canTakeStackFromSlot(ItemStack stack) {
			return true;
		}

		@Override
		public ItemStack decreaseStackInSlot() {
			return inv.decrStackSize(slot, 1);
		}

		@Override
		public int getIndex() {
			return slot;
		}
	}
}
