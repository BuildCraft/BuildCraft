/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.misc;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import buildcraft.lib.compat.forge_stubs.OreDictionaryStub;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

// Ported to Fabric 1.20.1 by R.Chen:
//   - DefaultedList → DefaultedList (create() → of(), withSize() → ofSize()).
//   - ItemStack.areItemsEqual + areItemStackTagsEqual → ItemStack.canCombine.
//   - getMaxStackSize/getCount/grow → getMaxCount/getCount/increment.
//   - serializeNBT()/getMetadata() → getNbt() (Yarn).
// STUB(R.Chen): the OreDictionaryStub-, BlockState-, IngredientStack-/StackDefinition-, and IList-based
//   helpers (isCraftingEquivalent, getItemStackForState, isMatchingItem variants, contains(StackDefinition…),
//   registerMatchingPredicate, stripNonFunctionNbt, …) are dropped until the Forge OreDictionaryStub →
//   Fabric tag layer and api.recipes/api.items are migrated (Phase 4E).
/** Provides various utils for interacting with {@link ItemStack}, and multiples. */
public class StackUtil {

    /** A non-null version of {@link ItemStack#EMPTY}. When the original field adds an @Nonnull annotation this should
     * be inlined. */
    @Nonnull
    public static final ItemStack EMPTY;

    static {
        ItemStack stack = ItemStack.EMPTY;
        if (stack == null) throw new NullPointerException("Empty ItemStack was null!");
        EMPTY = stack;
    }

    /** Checks to see if the two input stacks are equal in all but stack size. Note that this doesn't check anything
     * todo with stack size, so if you pass in two stacks of 64 cobblestone this will return true. */
    public static boolean canMerge(@Nonnull ItemStack a, @Nonnull ItemStack b) {
        // Checks item identity and NBT (count is ignored).
        return ItemStack.canCombine(a, b);
    }

    /** Checks to see if the given required stack is contained fully in the given container stack. */
    public static boolean contains(@Nonnull ItemStack required, @Nonnull ItemStack container) {
        if (canMerge(required, container)) {
            return container.getCount() >= required.getCount();
        }
        return false;
    }

    /** Checks to see if the given required stack is contained fully in a single stack in a list. */
    public static boolean contains(@Nonnull ItemStack required, Collection<ItemStack> containers) {
        for (ItemStack possible : containers) {
            if (possible == null) {
                throw new NullPointerException("Found a null itemstack in " + containers);
            }
            if (contains(required, possible)) {
                return true;
            }
        }
        return false;
    }

    /** Checks to see if the given required stacks are all contained within the collection of containers. Note that this
     * assumes that all of the required stacks are different. */
    public static boolean containsAll(Collection<ItemStack> required, Collection<ItemStack> containers) {
        for (ItemStack req : required) {
            if (req == null) {
                throw new NullPointerException("Found a null itemstack in " + containers);
            }
            if (req.isEmpty()) continue;
            if (!contains(req, containers)) {
                return false;
            }
        }
        return true;
    }

    /** This doesn't take into account stack sizes.
     *
     * @param filterOrList The exact itemstack to test.
     * @param test The stack to test for equality
     * @return True if they matched, or false if they didn't, or either was empty. */
    public static boolean matchesStackOrList(@Nonnull ItemStack filterOrList, @Nonnull ItemStack test) {
        if (filterOrList.isEmpty() || test.isEmpty()) {
            return false;
        }
        // STUB(R.Chen): the IList (buildcraft.api.items.IList) branch is dropped until api.items is migrated.
        return canMerge(filterOrList, test);
    }

    /** Merges mergeSource into mergeTarget
     *
     * @param mergeSource - The stack to merge into mergeTarget, this stack is not modified
     * @param mergeTarget - The target merge, this stack is modified if doMerge is set
     * @param doMerge - To actually do the merge
     * @return The number of items that was successfully merged. */
    public static int mergeStacks(@Nonnull ItemStack mergeSource, @Nonnull ItemStack mergeTarget, boolean doMerge) {
        if (!canMerge(mergeSource, mergeTarget)) {
            return 0;
        }
        int mergeCount = Math.min(mergeTarget.getMaxCount() - mergeTarget.getCount(), mergeSource.getCount());
        if (mergeCount < 1) {
            return 0;
        }
        if (doMerge) {
            mergeTarget.setCount(mergeTarget.getCount() + mergeCount);
        }
        return mergeCount;
    }

    /** @return An empty, nonnull list that cannot be modified (as it cannot be expanded and it has a size of 0) */
    public static DefaultedList<ItemStack> listOf() {
        return DefaultedList.ofSize(0, EMPTY);
    }

    /** Creates a {@link DefaultedList} of {@link ItemStack}'s with the elements given in the order that they are given.
     *
     * @param stacks The stacks to put into a list
     * @return A {@link DefaultedList} of all the given items. Note that the returned list is of a specified size, and
     *         cannot be expanded. */
    public static DefaultedList<ItemStack> listOf(ItemStack... stacks) {
        switch (stacks.length) {
            case 0:
                return listOf();
            case 1:
                return DefaultedList.ofSize(1, stacks[0]);
            default:
        }
        DefaultedList<ItemStack> list = DefaultedList.ofSize(stacks.length, EMPTY);
        for (int i = 0; i < stacks.length; i++) {
            list.set(i, stacks[i]);
        }
        return list;
    }

    /** Takes a {@link Nullable} {@link Object} and checks to make sure that it is really {@link Nonnull}.
     *
     * @param obj The (potentially) null object.
     * @return A {@link Nonnull} object, which will be the input object
     * @throws NullPointerException if the input object was actually null. */
    @Nonnull
    public static <T> T asNonNull(@Nullable T obj) {
        if (obj == null) {
            throw new NullPointerException("Object was null!");
        }
        return obj;
    }

    @Nonnull
    public static <T> T asNonNullSoft(@Nullable T obj, @Nonnull T fallback) {
        if (obj == null) {
            return fallback;
        } else {
            return obj;
        }
    }

    @Nonnull
    public static ItemStack asNonNullSoft(@Nullable ItemStack stack) {
        return asNonNullSoft(stack, EMPTY);
    }

    /** @return A {@link Collector} that will collect the input elements into a {@link DefaultedList} */
    public static <E> Collector<E, ?, DefaultedList<E>> nonNullListCollector() {
        return Collectors.toCollection(DefaultedList::of);
    }

    /** Computes a hash code for the given {@link ItemStack}, based off its item identity and NBT. Empty stacks hash
     * to 0. */
    public static int hash(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getNbt() == null) {
            return Objects.hash(stack.getItem());
        }
        return Objects.hash(stack.getItem(), stack.getNbt());
    }

    public static DefaultedList<ItemStack> mergeSameItems(List<ItemStack> items) {
        DefaultedList<ItemStack> stacks = DefaultedList.of();
        for (ItemStack toAdd : items) {
            boolean found = false;
            for (ItemStack stack : stacks) {
                if (canMerge(stack, toAdd)) {
                    stack.increment(toAdd.getCount());
                    found = true;
                }
            }
            if (!found) {
                stacks.add(toAdd.copy());
            }
        }
        return stacks;
    }

    /** Returns true if either stack matches the other (same item, same NBT). */
    public static boolean doesEitherStackMatch(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        return ItemStack.areEqual(a, b) || ItemStack.areItemsEqual(a, b);
    }

    // STUB(R.Chen): Forge ore-dictionary / IList matching — simplified to exact item match until ported.
    public static boolean isMatchingItem(ItemStack filter, ItemStack test, boolean matchDamage, boolean matchNbt) {
        if (filter.isEmpty() || test.isEmpty()) return false;
        if (filter.getItem() != test.getItem()) return false;
        if (matchNbt && !ItemStack.areNbtEqual(filter, test)) return false;
        return true;
    }
    public static boolean isMatchingItemOrList(ItemStack filter, ItemStack test) {
        return isMatchingItem(filter, test, false, false);
    }
    public static boolean canStacksOrListsMerge(ItemStack a, ItemStack b) { return canMerge(a, b); }

    /** Sentinel constant for search-mode item matching. */
    public static final ItemStack SEARCH = ItemStack.EMPTY;
}
