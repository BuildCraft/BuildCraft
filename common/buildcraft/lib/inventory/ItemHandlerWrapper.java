/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public final class ItemHandlerWrapper extends AbstractInvItemTransactor {
    private final IItemHandler wrapped;

    public ItemHandlerWrapper(IItemHandler handler) {
        this.wrapped = handler;
    }

    @Nonnull
    @Override
    protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return wrapped.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        if (min <= 0) min = 1;
        if (max < min) return StackUtil.EMPTY;
        ItemStack current = wrapped.getStackInSlot(slot);
        if (current.isEmpty() || current.getCount() < min) return StackUtil.EMPTY;
        if (filter.matches(asValid(current))) {
            return wrapped.extractItem(slot, max, simulate);
        }
        return StackUtil.EMPTY;
    }

    @Override
    protected int getSlots() {
        return wrapped.getSlots();
    }

    @Override
    protected boolean isEmpty(int slot) {
        return wrapped.getStackInSlot(slot).isEmpty();
    }
}
