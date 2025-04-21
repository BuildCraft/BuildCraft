/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.inventory;

import buildcraft.api.core.IInvSlot;
import buildcraft.lib.misc.InventoryUtil;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Iterator;

class InventoryIteratorSimple implements Iterable<IInvSlot> {

    private final Container inv;

    InventoryIteratorSimple(Container inv) {
        this.inv = InventoryUtil.getInventory(inv);
    }

    @Override
    public Iterator<IInvSlot> iterator() {
        return new Iterator<IInvSlot>() {
            int slot = 0;

            @Override
            public boolean hasNext() {
                // return slot < inv.getSizeInventory();
                return slot < inv.getContainerSize();
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

        @Nonnull
        @Override
        public ItemStack getStackInSlot() {
            // return inv.getStackInSlot(slot);
            return inv.getItem(slot);
        }

        @Override
        public void setStackInSlot(@Nonnull ItemStack stack) {
            // inv.setInventorySlotContents(slot, stack);
            inv.setItem(slot, stack);
        }

        @Override
        public boolean canPutStackInSlot(ItemStack stack) {
            // return inv.isItemValidForSlot(slot, stack);
            return inv.canPlaceItem(slot, stack);
        }

        @Override
        public boolean canTakeStackFromSlot(ItemStack stack) {
            return true;
        }

        @Override
        public boolean isItemValidForSlot(ItemStack stack) {
            // return inv.isItemValidForSlot(slot, stack);
            return inv.canPlaceItem(slot, stack);
        }

        @Nonnull
        @Override
        public ItemStack decreaseStackInSlot(int amount) {
            // return inv.decrStackSize(slot, amount);
            return inv.removeItem(slot, amount);
        }

        @Override
        public int getIndex() {
            return slot;
        }
    }
}
