/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.IStackFilter;
import ct.buildcraft.lib.misc.StackUtil;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class InventoryWrapper extends AbstractInvItemTransactor {
    private final Container Container;

    public InventoryWrapper(Container Container) {
        this.Container = Container;
    }

    @Override
    @Nonnull
    protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack current = Container.getItem(slot);
        if (!Container.canPlaceItem(slot, stack)) {
            return stack;
        }
        if (current.isEmpty()) {
            int max = Math.min(Container.getContainerSize(), stack.getMaxStackSize());
            ItemStack split = stack.split(max);
            if (!simulate) {
                Container.setItem(slot, split);
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
            int size = Math.min(Container.getContainerSize(), merged.getMaxStackSize());
            if (merged.getCount() > size) {
                stack.setCount(stack.getCount() - (merged.getCount() - size));
                merged.setCount(size);
                if (!simulate) {
                    Container.setItem(slot, merged);
                }
                return stack;
            } else {
                if (!simulate) {
                    Container.setItem(slot, merged);
                }
                return StackUtil.EMPTY;
            }
        }
        return stack;
    }

    @Override
    @Nonnull
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        ItemStack current = Container.getItem(slot);
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
                Container.setItem(slot, current);
            }
            return other;
        } else {
            return StackUtil.EMPTY;
        }
    }

    @Override
    protected int getSlots() {
        return Container.getContainerSize();
    }

    @Override
    protected boolean isEmpty(int slot) {
        return Container.getItem(slot).isEmpty();
    }
}
