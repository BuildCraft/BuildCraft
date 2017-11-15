/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ReportedException;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.IStackFilter;

import buildcraft.lib.inventory.AbstractInvItemTransactor;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.StackInsertionFunction.InsertionResult;

public class ItemHandlerSimple extends AbstractInvItemTransactor
    implements IItemHandlerModifiable, IItemHandlerAdv, INBTSerializable<NBTTagCompound> {
    // Function-called stuff (helpers etc)
    private StackInsertionChecker checker;
    private StackInsertionFunction inserter;

    @Nullable
    private StackChangeCallback callback;

    // Actual item stacks used
    public final NonNullList<ItemStack> stacks;

    // Transactor speedup (small)
    private int firstUsed = Integer.MAX_VALUE;

    public ItemHandlerSimple(int size) {
        this(size, (slot, stack) -> true, StackInsertionFunction.getDefaultInserter(), null);
    }

    public ItemHandlerSimple(int size, int maxStackSize) {
        this(size);
        setLimitedInsertor(maxStackSize);
    }

    public ItemHandlerSimple(int size, @Nullable StackChangeCallback callback) {
        this(size, (slot, stack) -> true, StackInsertionFunction.getDefaultInserter(), callback);
    }

    public ItemHandlerSimple(int size, StackInsertionChecker checker, StackInsertionFunction insertionFunction,
        @Nullable StackChangeCallback callback) {
        stacks = NonNullList.withSize(size, StackUtil.EMPTY);
        this.checker = checker;
        this.inserter = insertionFunction;
        this.callback = callback;
    }

    public void setChecker(StackInsertionChecker checker) {
        this.checker = checker;
    }

    public void setInsertor(StackInsertionFunction insertor) {
        this.inserter = insertor;
    }

    public void setLimitedInsertor(int maxStackSize) {
        setInsertor(StackInsertionFunction.getInsertionFunction(maxStackSize));
    }

    public void setCallback(StackChangeCallback callback) {
        this.callback = callback;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : stacks) {
            NBTTagCompound itemNbt = new NBTTagCompound();
            stack.writeToNBT(itemNbt);
            list.appendTag(itemNbt);
        }
        nbt.setTag("items", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount() && i < getSlots(); i++) {
            setStackInternal(i, StackUtil.EMPTY);
            ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
            // Obviously this can fail to load some items
            ItemStack leftOver = insert(i, stack, false);
            if (!leftOver.isEmpty()) {
                BCLog.logger.error("Failed to insert a stack while reading! (" + leftOver + ")", new Throwable());
            }
        }
        for (int i = list.tagCount(); i < getSlots(); i++) {
            setStackInternal(i, StackUtil.EMPTY);
        }
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    private boolean badSlotIndex(int slot) {
        return slot < 0 || slot >= stacks.size();
    }

    @Override
    protected boolean isEmpty(int slot) {
        if (badSlotIndex(slot)) return true;
        return stacks.get(slot).isEmpty();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        if (badSlotIndex(slot)) return StackUtil.EMPTY;
        return asValid(stacks.get(slot));
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (badSlotIndex(slot)) {
            return stack;
        }
        if (canSet(slot, stack)) {
            ItemStack current = stacks.get(slot);
            InsertionResult result = inserter.modifyForInsertion(slot, asValid(current.copy()), asValid(stack.copy()));
            if (!canSet(slot, result.toSet)) {
                // We have a bad inserter or checker, as they should not be conflicting
                CrashReport report = new CrashReport("Inserting an item (buildcraft:ItemHandlerSimple)",
                    new IllegalStateException("Conflicting Insertion!"));
                CrashReportCategory cat = report.makeCategory("Inventory details");
                cat.addCrashSection("Existing Item", current);
                cat.addCrashSection("Inserting Item", stack);
                cat.addCrashSection("Slot", slot);
                cat.addCrashSection("Checker", checker.getClass());
                cat.addCrashSection("Inserter", inserter.getClass());
                throw new ReportedException(report);
            } else if (!simulate) {
                setStackInternal(slot, result.toSet);
            }
            return asValid(result.toReturn);
        } else {
            return stack;
        }
    }

    @Override
    @Nonnull
    protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return insertItem(slot, stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (badSlotIndex(slot)) return StackUtil.EMPTY;
        // You can ALWAYS extract. if you couldn't then you could never take out items from anywhere
        ItemStack current = stacks.get(slot);
        if (current.isEmpty()) return StackUtil.EMPTY;
        if (current.getCount() < amount) {
            if (simulate) {
                return asValid(current.copy());
            }
            setStackInternal(slot, StackUtil.EMPTY);
            if (callback != null) {
                callback.onStackChange(this, slot, current, StackUtil.EMPTY);
            }
            // no need to copy as we no longer have it
            return current;
        } else {
            ItemStack before = current;
            current = current.copy();
            ItemStack split = current.splitStack(amount);
            if (!simulate) {
                if (current.getCount() <= 0) current = StackUtil.EMPTY;
                setStackInternal(slot, current);
                if (callback != null) {
                    callback.onStackChange(this, slot, before, current);
                }
            }
            return split;
        }
    }

    @Override
    @Nonnull
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        if (badSlotIndex(slot)) return StackUtil.EMPTY;
        if (min <= 0) min = 1;
        if (max < min) return StackUtil.EMPTY;
        ItemStack current = stacks.get(slot);
        ItemStack before = current.copy();
        if (current.getCount() < min) return StackUtil.EMPTY;
        if (filter.matches(asValid(current))) {
            if (simulate) {
                ItemStack copy = current.copy();
                return copy.splitStack(max);
            }
            ItemStack split = current.splitStack(max);
            if (current.getCount() <= 0) {
                stacks.set(slot, StackUtil.EMPTY);
            }
            if (callback != null) {
                callback.onStackChange(this, slot, before, stacks.get(slot));
            }
            return split;
        }
        return StackUtil.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (badSlotIndex(slot)) {
            // Its safe to throw here
            throw new IndexOutOfBoundsException("Slot index out of range: " + slot);
        }
        if (canSet(slot, stack)) {
            ItemStack before = stacks.get(slot);
            setStackInternal(slot, stack);
            if (callback != null) {
                callback.onStackChange(this, slot, before, asValid(stack));
            }
        } else {
            // Someone miss-called this. Woops. Looks like they didn't call insert.
            // If this is *somehow* called from vanilla code then its probably a vanilla bug
            throw new IllegalStateException(
                "Attempted to set stack[" + slot + "] when it was invalid! (" + stack + ")");
        }
    }

    @Override
    public final boolean canSet(int slot, @Nonnull ItemStack stack) {
        ItemStack copied = asValid(stack);
        if (copied.isEmpty()) {
            return true;
        }
        return checker.canSet(slot, copied);
    }

    private void setStackInternal(int slot, @Nonnull ItemStack stack) {
        stacks.set(slot, asValid(stack));
        // Transactor calc
        if (stack.isEmpty() && firstUsed == slot) {
            for (int s = firstUsed; s < getSlots(); s++) {
                if (!stacks.get(s).isEmpty()) {
                    firstUsed = s;
                    break;
                }
            }
            if (firstUsed == slot) {
                firstUsed = Integer.MAX_VALUE;
            }
        } else if (!stack.isEmpty() && firstUsed > slot) {
            firstUsed = slot;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public String toString() {
        return "ItemHandlerSimple " + stacks;
    }
}
