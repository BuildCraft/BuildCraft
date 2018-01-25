/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import javax.annotation.Nullable;

import buildcraft.lib.item.ItemStackHelper;
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
    @Nullable
    protected ItemStack insert(int slot, @Nullable ItemStack stack, boolean simulate) {
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
            if (ItemStackHelper.isEmpty(stack)) {
                return null;
            } else {
                return stack;
            }
        }
        if (StackUtil.canMerge(current, stack)) {
            ItemStack merged = current.copy();
            merged.stackSize += stack.stackSize;
            int size = Math.min(inventory.getInventoryStackLimit(), merged.getMaxStackSize());
            if (merged.stackSize > size) {
                stack.stackSize = (stack.stackSize - (merged.stackSize - size));
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
    @Nullable
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        ItemStack current = inventory.getStackInSlot(slot);
        if (ItemStackHelper.isEmpty(current)) {
            return null;
        }
        if (filter.matches(current.copy())) {
            if (current.stackSize < min) {
                return null;
            }
            int size = Math.min(current.stackSize, max);
            current = current.copy();
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
