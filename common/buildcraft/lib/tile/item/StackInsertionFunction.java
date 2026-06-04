/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

/** Defines a way of inserting items into an inventory — overridable for custom stack-merge rules. */
@FunctionalInterface
public interface StackInsertionFunction {

    @Nonnull
    InsertionResult modifyForInsertion(int slot, @Nonnull ItemStack addingTo, @Nonnull ItemStack toInsert);

    static StackInsertionFunction getInsertionFunction(int maxStackSize) {
        return (slot, addingTo, toInsert) -> {
            if (toInsert.isEmpty()) {
                return new InsertionResult(addingTo, ItemStack.EMPTY);
            }
            if (addingTo.isEmpty()) {
                int maxSize = Math.min(maxStackSize, toInsert.getMaxCount());
                if (toInsert.getCount() <= maxSize) {
                    return new InsertionResult(toInsert, ItemStack.EMPTY);
                } else {
                    // toInsert.copy().split(maxSize) removes maxSize from the copy and returns it
                    ItemStack mutable = toInsert.copy();
                    ItemStack inserted = mutable.split(maxSize);
                    return new InsertionResult(inserted, mutable);
                }
            } else if (addingTo.getCount() == maxStackSize) {
                return new InsertionResult(addingTo, toInsert);
            } else if (ItemStack.canCombine(addingTo, toInsert)) {
                ItemStack complete = addingTo.copy();
                int count = addingTo.getCount() + toInsert.getCount();
                int maxSize = Math.min(maxStackSize, complete.getMaxCount());
                if (count <= maxSize) {
                    complete.setCount(count);
                    return new InsertionResult(complete, ItemStack.EMPTY);
                } else {
                    complete.setCount(maxSize);
                    ItemStack leftOver = toInsert.copy();
                    leftOver.setCount(count - maxSize);
                    return new InsertionResult(complete, leftOver);
                }
            }
            return new InsertionResult(addingTo, toInsert);
        };
    }

    static StackInsertionFunction getDefaultInserter() {
        return getInsertionFunction(Integer.MAX_VALUE);
    }

    class InsertionResult {
        public static final InsertionResult EMPTY_STACKS = new InsertionResult(ItemStack.EMPTY, ItemStack.EMPTY);

        @Nonnull
        public final ItemStack toSet, toReturn;

        public InsertionResult(@Nonnull ItemStack toSet, @Nonnull ItemStack toReturn) {
            this.toSet = toSet;
            this.toReturn = toReturn;
        }
    }
}
