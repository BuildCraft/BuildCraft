/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public final class InventoryWrapper extends AbstractInvItemTransactor {
    private final Container inventory;

    public InventoryWrapper(Container inventory) {
        this.inventory = inventory;
    }

    @Override
    @Nonnull
    protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack current = inventory.getItem(slot);
        if (!inventory.canPlaceItem(slot, stack)) {
            return stack;
        }
        if (current.isEmpty()) {
            int max = Math.min(inventory.getMaxStackSize(), stack.getMaxStackSize());
            ItemStack split = stack.split(max);
            if (!simulate) {
                inventory.setItem(slot, split);
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
            int size = Math.min(inventory.getMaxStackSize(), merged.getMaxStackSize());
            if (merged.getCount() > size) {
                stack.setCount(stack.getCount() - (merged.getCount() - size));
                merged.setCount(size);
                if (!simulate) {
                    inventory.setItem(slot, merged);
                }
                return stack;
            } else {
                if (!simulate) {
                    inventory.setItem(slot, merged);
                }
                return StackUtil.EMPTY;
            }
        }
        return stack;
    }

    @Override
    @Nonnull
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        ItemStack current = inventory.getItem(slot);
        if (current.isEmpty()) {
            return StackUtil.EMPTY;
        }
        if (filter.matches(current.copy())) {
            if (current.getCount() < min) {
                return StackUtil.EMPTY;
            }
            int size = Math.min(current.getCount(), max);
            current = current.copy();
            ItemStack other = current.split(size);
            if (!simulate) {
                if (current.getCount() <= 0) {
                    current = StackUtil.EMPTY;
                }
                inventory.setItem(slot, current);
            }
            return other;
        } else {
            return StackUtil.EMPTY;
        }
    }

    @Override
    protected int getSlots() {
        return inventory.getContainerSize();
    }

    @Override
    protected boolean isEmpty(int slot) {
        return inventory.getItem(slot).isEmpty();
    }
}
