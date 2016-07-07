package buildcraft.lib.inventory;

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
    protected ItemStack insert(int slot, ItemStack stack, boolean simulate) {
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
            if (stack.stackSize > 0) {
                return stack;
            } else {
                return null;
            }
        }
        if (StackUtil.canMerge(current, stack)) {
            ItemStack merged = current.copy();
            merged.stackSize += stack.stackSize;
            int size = Math.min(inventory.getInventoryStackLimit(), merged.getMaxStackSize());
            if (merged.stackSize > size) {
                stack.stackSize -= merged.stackSize - size;
                merged.stackSize = size;
                if (!simulate) {
                    inventory.setInventorySlotContents(slot, merged);
                }
                return stack;
            } else {
                if (!simulate) {
                    inventory.setInventorySlotContents(slot, merged);
                }
                return null;
            }
        }
        return stack;
    }

    @Override
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        ItemStack current = inventory.getStackInSlot(slot);
        if (current == null) {
            return null;
        }
        if (filter.matches(current.copy())) {
            if (current.stackSize < min) {
                return null;
            }
            int size = Math.min(current.stackSize, max);
            ItemStack other = current.splitStack(size);
            if (!simulate) {
                if (current.stackSize <= 0) {
                    current = null;
                }
                inventory.setInventorySlotContents(slot, current);
            }
            return other;
        } else {
            return null;
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