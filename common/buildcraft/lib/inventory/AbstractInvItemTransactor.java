/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.items.BCStackHelper;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.StackUtil;
import gnu.trove.list.array.TIntArrayList;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Designates an {@link IItemTransactor} that is backed by a simple, static, array based inventory. */
public abstract class AbstractInvItemTransactor implements IItemTransactor {
    /** Returns null if it was empty, or the input stack if it was not. */
    @Nullable
    public static ItemStack asValid(@Nullable ItemStack stack) {
        if (BCStackHelper.isEmpty(stack)) {
            return null;
        } else {
            return stack;
        }
    }

    @Nullable
    protected abstract ItemStack insert(int slot, @Nullable ItemStack stack, boolean simulate);

    @Nullable
    protected abstract ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate);

    protected abstract int getSlots();

    protected abstract boolean isEmpty(int slot);

    @Override
    @Nullable
    public ItemStack insert(@Nullable ItemStack stack, boolean allAtOnce, boolean simulate) {
        if (allAtOnce) {
            return insertAllAtOnce(stack, simulate);
        } else {
            return insertAnyAmount(stack, simulate);
        }
    }

    @Nullable
    private ItemStack insertAnyAmount(@Nullable ItemStack stack, boolean simulate) {
        int slotCount = getSlots();
        TIntArrayList emptySlots = new TIntArrayList(slotCount);
        for (int slot = 0; slot < getSlots(); slot++) {
            if (isEmpty(slot)) {
                emptySlots.add(slot);
            } else {
                stack = insert(slot, stack, simulate);
                if (BCStackHelper.isEmpty(stack)) return null;
            }
        }
        for (int slot : emptySlots.toArray()) {
            stack = insert(slot, stack, simulate);
            if (BCStackHelper.isEmpty(stack)) return null;
        }
        return stack;
    }

    @Nullable
    private ItemStack insertAllAtOnce(@Nullable ItemStack stack, boolean simulate) {
        ItemStack before = asValid(stack);
        TIntArrayList insertedSlots = new TIntArrayList(getSlots());
        TIntArrayList emptySlots = new TIntArrayList(getSlots());
        for (int slot = 0; slot < getSlots(); slot++) {
            if (isEmpty(slot)) {
                emptySlots.add(slot);
            } else {
                stack = insert(slot, stack, true);
                insertedSlots.add(slot);
                if (BCStackHelper.isEmpty(stack)) break;
            }
        }
        for (int slot : emptySlots.toArray()) {
            stack = insert(slot, stack, true);
            insertedSlots.add(slot);
            if (BCStackHelper.isEmpty(stack)) break;
        }
        if (!BCStackHelper.isEmpty(stack)) {
            return stack;
        }
        if (simulate) return null;
        for (int slot : insertedSlots.toArray()) {
            before = insert(slot, before, false);
        }
        if (!BCStackHelper.isEmpty(before)) {
            // We have a bad implementation that doesn't respect simulation properly- we are in an invalid state at this
            // point with no chance of recovery
            throw new IllegalStateException("Somehow inserting a lot of items at once failed when we thought it shouldn't! ("
                    + getClass() + ")");
        }
        return null;
    }

    @Override
    public List<ItemStack> insert(List<ItemStack> stacks, boolean simulate) {
        // WARNING: SLOW IMPL
        return stacks;
    }

    @Nullable
    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (min < 1) min = 1;
        if (min > max) return null;
        if (max < 0) return null;

        if (Objects.isNull(filter)) {
            filter = StackFilter.ALL;
        }

        int slots = getSlots();
        TIntArrayList valids = new TIntArrayList();
        int totalSize = 0;
        ItemStack toExtract = null;

        for (int slot = 0; slot < slots; slot++) {
            ItemStack possible = extract(slot, filter, 1, max - totalSize, true);
            if (!BCStackHelper.isEmpty(possible)) {
                if (BCStackHelper.isEmpty(toExtract)) {
                    toExtract = possible.copy();
                }
                if (StackUtil.canMerge(toExtract, possible)) {
                    totalSize += possible.stackSize;
                    valids.add(slot);
                    if (totalSize >= max) {
                        break;
                    }
                }
            }
        }
        if (Objects.isNull(toExtract))
            return null;

        ItemStack total = null;
        if (min <= totalSize) {
            for (int slot : valids.toArray()) {
                ItemStack extracted = extract(slot, filter, 1, max - total.stackSize, simulate);
                if (!BCStackHelper.isEmpty(extracted)) {
                    if (BCStackHelper.isEmpty(total)) {
                        total = extracted.copy();
                    } else {
                        total.stackSize += extracted.stackSize;
                    }
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
