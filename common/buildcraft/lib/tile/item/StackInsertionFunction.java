/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.item;

import buildcraft.lib.misc.StackUtil;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/** Defines a way of inserting items into an inventory - this can be overridden to have different rules with stack
 * merging. */
@FunctionalInterface
public interface StackInsertionFunction {
    /** @param addingTo The existing stack. Modifications are lost.
     * @param toInsert The stacks to insert. Modifications are lost.
     * @return The result of attempting to insert it. */
    @Nonnull
    InsertionResult modifyForInsertion(int slot, @Nonnull ItemStack addingTo, @Nonnull ItemStack toInsert);

    /** Gets a stack insertion function that will insert items up to a given stack size. The stack size of the items
     * themselves IS taken into account, so this has an effective upper limit of 64. */
    static StackInsertionFunction getInsertionFunction(int maxStackSize) {
        return (slot, addingTo, toInsert) ->
        {
            if (toInsert.isEmpty()) {
                return new InsertionResult(addingTo, StackUtil.EMPTY);
            }

            if (addingTo.isEmpty()) {
                int maxSize = Math.min(maxStackSize, toInsert.getMaxStackSize());
                if (toInsert.getCount() <= maxSize) {
                    return new InsertionResult(toInsert, StackUtil.EMPTY);
                } else {
                    ItemStack inserted = toInsert.split(maxSize);
                    return new InsertionResult(inserted, toInsert);
                }
            } else if (addingTo.getCount() == maxStackSize) {
                return new InsertionResult(addingTo, toInsert);
            } else if (StackUtil.canMerge(addingTo, toInsert)) {
                ItemStack complete = addingTo.copy();
                int count = addingTo.getCount() + toInsert.getCount();
                int maxSize = Math.min(maxStackSize, complete.getMaxStackSize());
                if (count <= maxSize) {
                    complete.setCount(count);
                    return new InsertionResult(complete, StackUtil.EMPTY);
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

    /** Gets a stack insertion function that will insert up to full stacks into a given slot. This is just
     * {@link #getInsertionFunction(int)} with an argument of {@link Integer#MAX_VALUE}. */
    public static StackInsertionFunction getDefaultInserter() {
        return getInsertionFunction(Integer.MAX_VALUE);
    }

    /** The result of an attempted insertion. */
    class InsertionResult {
        public static final InsertionResult EMPTY_STACKS = new InsertionResult(StackUtil.EMPTY, StackUtil.EMPTY);

        @Nonnull
        public final ItemStack toSet, toReturn;

        public InsertionResult(@Nonnull ItemStack toSet, @Nonnull ItemStack toReturn) {
            this.toSet = toSet;
            this.toReturn = toReturn;
        }
    }
}
