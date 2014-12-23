/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import java.util.Iterator;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IInvSlot;

class InventoryIteratorSided implements Iterable<IInvSlot> {

    private final ISidedInventory inv;
    private final EnumFacing side;

    InventoryIteratorSided(ISidedInventory inv, EnumFacing side) {
        this.inv = inv;
		this.side = side;
    }

    @Override
    public Iterator<IInvSlot> iterator() {
        return new Iterator<IInvSlot>() {
            int[] slots = inv.getSlotsForFace(side);
            int index = 0;

            @Override
            public boolean hasNext() {
                return slots != null ? index < slots.length : false;
            }

            @Override
            public IInvSlot next() {
                return slots != null ? new InvSlot(slots[index++]) : null;
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
            return inv.canInsertItem(slot, stack, side);
        }

        @Override
        public boolean canTakeStackFromSlot(ItemStack stack) {
            return inv.canExtractItem(slot, stack, side);
        }

        @Override
		public ItemStack decreaseStackInSlot(int amount) {
			return inv.decrStackSize(slot, amount);
        }

		@Override
		public int getIndex() {
			return slot;
		}

    }
}
