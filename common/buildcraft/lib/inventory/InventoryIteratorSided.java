/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.inventory;

import buildcraft.api.core.IInvSlot;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Iterator;

class InventoryIteratorSided implements Iterable<IInvSlot> {

    // private final ISidedInventory inv;
    private final WorldlyContainer inv;
    private final Direction side;

    // InventoryIteratorSided(ISidedInventory inv, EnumFacing side)
    InventoryIteratorSided(WorldlyContainer inv, Direction side) {
        this.inv = inv;
        this.side = side;
    }

    @Override
    public Iterator<IInvSlot> iterator() {
        return new Iterator<IInvSlot>() {
            @Nonnull
            int[] slots = inv.getSlotsForFace(side);
            int index = 0;

            @Override
            public boolean hasNext() {
                // return slots != null ? index < slots.length : false;
                return slots.length != 0 ? index < slots.length : false;
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
            // return inv.canInsertItem(slot, stack, side) && inv.isItemValidForSlot(slot, stack);
            return inv.canPlaceItemThroughFace(slot, stack, side) && inv.canPlaceItem(slot, stack);
        }

        @Override
        public boolean canTakeStackFromSlot(ItemStack stack) {
            // return inv.canExtractItem(slot, stack, side);
            return inv.canTakeItemThroughFace(slot, stack, side);
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
