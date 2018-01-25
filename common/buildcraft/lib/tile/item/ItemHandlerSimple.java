/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ReportedException;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.IStackFilter;

import buildcraft.lib.inventory.AbstractInvItemTransactor;
import buildcraft.lib.tile.item.StackInsertionFunction.InsertionResult;

import java.util.List;

public class ItemHandlerSimple extends AbstractInvItemTransactor
    implements IItemHandlerModifiable, IItemHandlerAdv, INBTSerializable<NBTTagCompound> {
    // Function-called stuff (helpers etc)
    private StackInsertionChecker checker;
    private StackInsertionFunction inserter;

    @Nullable
    private StackChangeCallback callback;

    // Actual item stacks used
    public final List<ItemStack> stacks;

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
        stacks = Lists.newArrayListWithCapacity(size);
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
            if (stack != null) {
                NBTTagCompound itemNbt = new NBTTagCompound();
                stack.writeToNBT(itemNbt);
                list.appendTag(itemNbt);
            }
        }
        nbt.setTag("items", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount() && i < getSlots(); i++) {
            setStackInternal(i, ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
        }
        for (int i = list.tagCount(); i < getSlots(); i++) {
            setStackInternal(i, null);
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
        return badSlotIndex(slot) || stacks.get(slot) == null;
    }

    @Override
    @Nullable
    public ItemStack getStackInSlot(int slot) {
        if (badSlotIndex(slot)) return null;
        return stacks.get(slot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (badSlotIndex(slot)) {
            return stack;
        }
        if (canSet(slot, stack)) {
            ItemStack current = stacks.get(slot);
            if (!canSet(slot, current)) {
                // A bit odd, but can happen if the filter changed
                return stack;
            }
            InsertionResult result = inserter.modifyForInsertion(slot, current.copy(), stack.copy());
            if (!canSet(slot, result.toSet)) {
                // We have a bad inserter or checker, as they should not be conflicting
                CrashReport report = new CrashReport("Inserting an item (buildcraft:ItemHandlerSimple)",
                    new IllegalStateException("Conflicting Insertion!"));
                CrashReportCategory cat = report.makeCategory("Inventory details");
                cat.addCrashSection("Existing Item", current);
                cat.addCrashSection("Inserting Item", stack);
                cat.addCrashSection("To Set", result.toSet);
                cat.addCrashSection("To Return", result.toReturn);
                cat.addCrashSection("Slot", slot);
                cat.addCrashSection("Checker", checker.getClass());
                cat.addCrashSection("Inserter", inserter.getClass());
                throw new ReportedException(report);
            } else if (!simulate) {
                setStackInternal(slot, result.toSet);
                if (callback != null) {
                    callback.onStackChange(this, slot, current, result.toSet);
                }
            }
            return result.toReturn;
        } else {
            return stack;
        }
    }

    @Override
    protected ItemStack insert(int slot, ItemStack stack, boolean simulate) {
        return insertItem(slot, stack, simulate);
    }

    @Override
    @Nullable
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (badSlotIndex(slot)) return null;
        // You can ALWAYS extract. if you couldn't then you could never take out items from anywhere
        ItemStack current = stacks.get(slot);
        if (current == null) return null;
        if (current.stackSize < amount) {
            if (simulate) {
                return current.copy();
            }
            setStackInternal(slot, null);
            if (callback != null) {
                callback.onStackChange(this, slot, current, null);
            }
            // no need to copy as we no longer have it
            return current;
        } else {
            ItemStack before = current;
            current = current.copy();
            ItemStack split = current.splitStack(amount);
            if (!simulate) {
                if (current.stackSize <= 0) current = null;
                setStackInternal(slot, current);
                if (callback != null) {
                    callback.onStackChange(this, slot, before, current);
                }
            }
            return split;
        }
    }

    @Override
    @Nullable
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        if (badSlotIndex(slot)) return null;
        if (min <= 0) min = 1;
        if (max < min) return null;
        ItemStack current = stacks.get(slot);
        ItemStack before = current.copy();
        if (current.stackSize < min) return null;
        if (filter.matches(current)) {
            if (simulate) {
                ItemStack copy = current.copy();
                return copy.splitStack(max);
            }
            ItemStack split = current.splitStack(max);
            if (current.stackSize <= 0) {
                stacks.set(slot, null);
            }
            if (callback != null) {
                callback.onStackChange(this, slot, before, stacks.get(slot));
            }
            return split;
        }
        return null;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (badSlotIndex(slot)) {
            // Its safe to throw here
            throw new IndexOutOfBoundsException("Slot index out of range: " + slot);
        }
        ItemStack before = stacks.get(slot);
        setStackInternal(slot, stack);
        if (callback != null) {
            callback.onStackChange(this, slot, before, stack);
        }
    }

    @Override
    public final boolean canSet(int slot, ItemStack stack) {
        return stack == null || checker.canSet(slot, stack.copy());
    }

    private void setStackInternal(int slot, ItemStack stack) {
        stacks.set(slot, null);
        // Transactor calc
        if (stack == null && firstUsed == slot) {
            for (int s = firstUsed; s < getSlots(); s++) {
                if (stacks.get(s) != null) {
                    firstUsed = s;
                    break;
                }
            }
            if (firstUsed == slot) {
                firstUsed = Integer.MAX_VALUE;
            }
        } else if (stack != null && firstUsed > slot) {
            firstUsed = slot;
        }
    }

    @Override
    public String toString() {
        return "ItemHandlerSimple " + stacks;
    }
}
