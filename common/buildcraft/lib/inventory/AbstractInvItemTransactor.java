/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.StackUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;

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
            return insertAnyAmount(stack, simulate);
        }
    }

    @Nonnull
    private ItemStack insertAnyAmount(@Nonnull ItemStack stack, boolean simulate) {
        int slotCount = getSlots();
        IntArrayList emptySlots = new IntArrayList(slotCount);
        for (int slot = 0; slot < getSlots(); slot++) {
            if (isEmpty(slot)) {
                emptySlots.add(slot);
            } else {
                stack = insert(slot, stack, simulate);
                if (stack.isEmpty()) return StackUtil.EMPTY;
            }
        }
        for (int slot : emptySlots.toIntArray()) {
            stack = insert(slot, stack, simulate);
            if (stack.isEmpty()) return StackUtil.EMPTY;
        }
        return stack;
    }

    @Nonnull
    private ItemStack insertAllAtOnce(@Nonnull ItemStack stack, boolean simulate) {
        ItemStack before = asValid(stack);
        IntArrayList insertedSlots = new IntArrayList(getSlots());
        IntArrayList emptySlots = new IntArrayList(getSlots());
        for (int slot = 0; slot < getSlots(); slot++) {
            if (isEmpty(slot)) {
                emptySlots.add(slot);
            } else {
                stack = insert(slot, stack, true);
                insertedSlots.add(slot);
                if (stack.isEmpty()) break;
            }
        }
        for (int slot : emptySlots.toIntArray()) {
            stack = insert(slot, stack, true);
            insertedSlots.add(slot);
            if (stack.isEmpty()) break;
        }
        if (!stack.isEmpty()) {
            return stack;
        }
        if (simulate) return StackUtil.EMPTY;
        for (int slot : insertedSlots.toIntArray()) {
            before = insert(slot, before, false);
        }
        if (!before.isEmpty()) {
            // We have a bad implementation that doesn't respect simulation properly- we are in an invalid state at this
            // point with no chance of recovery
            throw new IllegalStateException("Somehow inserting a lot of items at once failed when we thought it shouldn't! ("
                    + getClass() + ")");
        }
        return StackUtil.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
        // WARNING: SLOW IMPL
        return stacks;
    }

    @Nonnull
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (min < 1) min = 1;
        if (min > max) return StackUtil.EMPTY;
        if (max < 0) return StackUtil.EMPTY;

        if (filter == null) {
            filter = StackFilter.ALL;
        }

        int slots = getSlots();
        IntArrayList valids = new IntArrayList();
        int totalSize = 0;
        ItemStack toExtract = StackUtil.EMPTY;

        for (int slot = 0; slot < slots; slot++) {
            ItemStack possible = extract(slot, filter, 1, max - totalSize, true);
            if (!possible.isEmpty()) {
                if (toExtract.isEmpty()) {
                    toExtract = possible.copy();
                }
                if (StackUtil.canMerge(toExtract, possible)) {
                    totalSize += possible.getCount();
                    valids.add(slot);
                    if (totalSize >= max) {
                        break;
                    }
                }
            }
        }

        ItemStack total = StackUtil.EMPTY;
        if (min <= totalSize) {
            for (int slot : valids.toIntArray()) {
                ItemStack extracted = extract(slot, filter, 1, max - total.getCount(), simulate);
                if (total.isEmpty()) {
                    total = extracted.copy();
                } else {
                    total.grow(extracted.getCount());
                }
            }
        }
        return total;
    }

    @Override
    public String toString() {
        ItemStack[] stacks = new ItemStack[getSlots()];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = extract(i, StackFilter.ALL, 1, Integer.MAX_VALUE, true);
        }
        return Arrays.toString(stacks);
    }
}
