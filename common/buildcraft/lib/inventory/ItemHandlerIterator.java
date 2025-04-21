package buildcraft.lib.inventory;

import buildcraft.api.core.IInvSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class ItemHandlerIterator implements Iterable<IInvSlot> {

    private final IItemHandler inv;

    ItemHandlerIterator(IItemHandler inv) {
        this.inv = inv;
    }

    @Override
    public Iterator<IInvSlot> iterator() {
        return new Iterator<IInvSlot>() {
            int slot = 0;

            @Override
            public boolean hasNext() {
                // return slot < inv.getSizeInventory();
                return slot < inv.getSlots();
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
            return inv.getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(@Nonnull ItemStack stack) {
            // inv.setInventorySlotContents(slot, stack);
            inv.extractItem(slot, inv.getStackInSlot(slot).getCount(), false);
            inv.insertItem(slot, stack, false);
        }

        @Override
        public boolean canPutStackInSlot(ItemStack stack) {
            // return inv.isItemValidForSlot(slot, stack);
            return inv.isItemValid(slot, stack);
        }

        @Override
        public boolean canTakeStackFromSlot(ItemStack stack) {
            return true;
        }

        @Override
        public boolean isItemValidForSlot(ItemStack stack) {
            // return inv.isItemValidForSlot(slot, stack);
            return inv.isItemValid(slot, stack);
        }

        @Nonnull
        @Override
        public ItemStack decreaseStackInSlot(int amount) {
            // return inv.decrStackSize(slot, amount);
            return inv.extractItem(slot, amount, false);
        }

        @Override
        public int getIndex() {
            return slot;
        }
    }
}
