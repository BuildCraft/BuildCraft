package buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;

import buildcraft.lib.misc.StackUtil;

public final class InventoryWrapper extends AbstractInvItemTransactor {
    private final IInventory inventory;

    public InventoryWrapper(IInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    @Nonnull
    protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack current = inventory.getStackInSlot(slot);
        if (!inventory.isItemValidForSlot(slot, stack)) {
            return stack;
        }
        if (current == null) {
            int max = Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());
            ItemStack split = stack.splitStack(max);
            if (!simulate) {
                inventory.setInventorySlotContents(slot, split);
            }
            if (stack.isEmpty()) {
                return StackUtil.EMPTY;
            } else {
                return stack;
            }
        }
        if (StackUtil.canMerge(current, stack)) {
            ItemStack merged = current.copy();
            merged.setCount(merged.getCount() + stack.getCount());
            int size = Math.min(inventory.getInventoryStackLimit(), merged.getMaxStackSize());
            if (merged.getCount() > size) {
                stack.setCount(stack.getCount() - (merged.getCount() - size));
                merged.setCount(size);
                if (!simulate) {
                    inventory.setInventorySlotContents(slot, merged);
                }
                return stack;
            } else {
                if (!simulate) {
                    inventory.setInventorySlotContents(slot, merged);
                }
                return StackUtil.EMPTY;
            }
        }
        return stack;
    }

    @Override
    @Nonnull
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        ItemStack current = inventory.getStackInSlot(slot);
        if (current == null) {
            return StackUtil.EMPTY;
        }
        if (filter.matches(current.copy())) {
            if (current.getCount() < min) {
                return StackUtil.EMPTY;
            }
            int size = Math.min(current.getCount(), max);
            current = current.copy();
            ItemStack other = current.splitStack(size);
            if (!simulate) {
                if (current.getCount() <= 0) {
                    current = null;
                }
                inventory.setInventorySlotContents(slot, current);
            }
            return other;
        } else {
            return StackUtil.EMPTY;
        }
    }

    @Override
    protected int getSlots() {
        return inventory.getSizeInventory();
    }

    @Override
    protected boolean isEmpty(int slot) {
        return inventory.getStackInSlot(slot) == null;
    }
}
