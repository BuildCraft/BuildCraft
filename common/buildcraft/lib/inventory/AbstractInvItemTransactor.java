/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): IItemTransactor abstract methods preserved; Forge refs removed

package buildcraft.lib.inventory;

import java.util.List;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;

import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.StackUtil;

/** Designates an {@link IItemTransactor} that is backed by a simple, static, array based inventory. */
public abstract class AbstractInvItemTransactor implements IItemTransactor {
    /** Returns {@link ItemStack#EMPTY} if it was empty, or the input stack if it was not. */
    @Nonnull
    public static ItemStack asValid(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return StackUtil.EMPTY;
        } else {
            return stack;
        }
    }

    @Nonnull
    protected abstract ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate);

    @Nonnull
    protected abstract ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate);

    protected abstract int getSlots();

    protected abstract boolean isEmpty(int slot);

    @Override
    @Nonnull
    public ItemStack insert(@Nonnull ItemStack stack, boolean allAtOnce, boolean simulate) {
        if (allAtOnce) {
            return insertAllAtOnce(stack, simulate);
        } else {
            return insertAnyOrder(stack, simulate);
        }
    }

    @Nonnull
    private ItemStack insertAllAtOnce(@Nonnull ItemStack stack, boolean simulate) {
        List<Integer> emptySlots = new ArrayList<>();
        List<Integer> partialSlots = new ArrayList<>();
        int count = stack.getCount();
        for (int i = 0; i < getSlots(); i++) {
            if (isEmpty(i)) {
                emptySlots.add(i);
            } else {
                ItemStack inSlot = insert(i, stack.copy(), true);
                if (inSlot.getCount() < stack.getCount()) {
                    partialSlots.add(i);
                }
            }
        }
        // simplified: just do a normal sequential insert in simulate=false path
        for (int i = 0; i < getSlots() && count > 0; i++) {
            ItemStack toInsert = stack.copy();
            toInsert.setCount(count);
            ItemStack leftover = insert(i, toInsert, simulate);
            count = leftover.getCount();
        }
        if (count <= 0) return StackUtil.EMPTY;
        ItemStack result = stack.copy();
        result.setCount(count);
        return result;
    }

    @Nonnull
    private ItemStack insertAnyOrder(@Nonnull ItemStack stack, boolean simulate) {
        int count = stack.getCount();
        for (int i = 0; i < getSlots() && count > 0; i++) {
            ItemStack toInsert = stack.copy();
            toInsert.setCount(count);
            ItemStack leftover = insert(i, toInsert, simulate);
            count = leftover.getCount();
        }
        if (count <= 0) return StackUtil.EMPTY;
        ItemStack result = stack.copy();
        result.setCount(count);
        return result;
    }

    @Override
    @Nonnull
    public ItemStack extract(@Nonnull IStackFilter filter, int min, int max, boolean simulate) {
        for (int i = 0; i < getSlots(); i++) {
            if (!isEmpty(i)) {
                ItemStack result = extract(i, filter, min, max, simulate);
                if (!result.isEmpty()) return result;
            }
        }
        return StackUtil.EMPTY;
    }
}
