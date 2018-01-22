/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

import buildcraft.api.core.IStackFilter;

public final class ItemHandlerWrapper extends AbstractInvItemTransactor {
    private final IItemHandler wrapped;

    public ItemHandlerWrapper(IItemHandler handler) {
        this.wrapped = handler;
    }

    @Override
    protected ItemStack insert(int slot, ItemStack stack, boolean simulate) {
        return wrapped.insertItem(slot, stack, simulate);
    }

    @Nullable
    @Override
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        if (min <= 0) min = 1;
        if (max < min) return null;
        ItemStack current = wrapped.getStackInSlot(slot);
        if (current == null || current.stackSize < min) return null;
        if (filter.matches(current)) {
            return wrapped.extractItem(slot, max, simulate);
        }
        return null;
    }

    @Override
    protected int getSlots() {
        return wrapped.getSlots();
    }

    @Override
    protected boolean isEmpty(int slot) {
        return wrapped.getStackInSlot(slot) == null;
    }
}
