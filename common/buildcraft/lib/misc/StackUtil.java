/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.items.IList;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.StackDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/** Provides various utils for interacting with {@link ItemStack}, and multiples. */
public class StackUtil {

    /** A non-null version of {@link ItemStack#EMPTY}. When the original field adds an @Nonnull annotation this should
     * be inlined. */
    // Actually the entire MC
    @Nonnull
    public static final ItemStack EMPTY;
    @Nonnull
    public static final FluidStack EMPTY_FLUID;

    /** Registry of additional rules for {@link #isMatchingItem}. */
    private static final Map<Item, List<StackMatchingPredicate>> matchingPredicates = new HashMap<>();

    static {
        ItemStack stack = ItemStack.EMPTY;
        FluidStack stackF = FluidStack.EMPTY;
        if (stack == null || stackF == null) throw new NullPointerException("Empty ItemStack was null!");
        EMPTY = stack;
        EMPTY_FLUID = stackF;
    }

    /** Checks to see if the two input stacks are equal in all but stack size. Note that this doesn't check anything
     * todo with stack size, so if you pass in two stacks of 64 cobblestone this will return true. If you pass in null
     * (at all) then this will only return true if both are null. */
    public static boolean canMerge(@Nonnull ItemStack a, @Nonnull ItemStack b) {
//        // Checks item, damage
//        if (!ItemStack.areItemsEqual(a, b)) {
//            return false;
//        }
//        // checks tags and caps
//        return ItemStack.areItemStackTagsEqual(a, b);
        return StackUtil.isSameItemSameDamageSameTag(a, b);
    }

    /** Attempts to get an item stack that might place down the given blockstate. Obviously this isn't perfect, and so
     * cannot be relied on for anything more than simple blocks. */
    @Nonnull
    public static ItemStack getItemStackForState(BlockState state) {
        Block b = state.getBlock();
//        ItemStack stack = new ItemStack(b);
//        if (stack.isEmpty()) {
//            return StackUtil.EMPTY;
//        }
//        if (stack.getHasSubtypes()) {
//            stack = new ItemStack(stack.getItem(), 1, b.getMetaFromState(state));
//        }
//        return stack;
        return b.getCloneItemStack(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, state);
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
                // Use an explicit null check here as the collection doesn't have @Nonnull applied to its type
                throw new NullPointerException("Found a null itemstack in " + containers);
            }
            if (contains(required, possible)) {
                return true;
            }
        }
        return false;
    }

    /** Checks that passed stack meets stack definition requirements */
    public static boolean contains(@Nonnull StackDefinition stackDefinition, @Nonnull ItemStack stack) {
        return !stack.isEmpty() && stackDefinition.filter.matches(stack) && stack.getCount() >= stackDefinition.count;
    }

    /** Checks that passed stack definition acceptable for stack collection */
    public static boolean contains(@Nonnull StackDefinition stackDefinition, @Nonnull NonNullList<ItemStack> stacks) {
        return stacks.stream().anyMatch((stack) -> contains(stackDefinition, stack));
    }

    /** Checks that passed stack meets stack definition requirements */
    public static boolean contains(@Nonnull IngredientStack ingredientStack, @Nonnull ItemStack stack) {
//        return !stack.isEmpty() && ingredientStack.ingredient.apply(stack) && stack.getCount() >= ingredientStack.count;
        return !stack.isEmpty() && ingredientStack.ingredient.test(stack) && stack.getCount() >= ingredientStack.count;
    }

    /** Checks that passed stack definition acceptable for stack collection */
    public static boolean contains(@Nonnull IngredientStack ingredientStack, @Nonnull NonNullList<ItemStack> stacks) {
        return stacks.stream().anyMatch((stack) -> contains(ingredientStack, stack));
    }

    /** Checks to see if the given required stacks are all contained within the collection of containers. Note that this
     * assumes that all of the required stacks are different. */
    public static boolean containsAll(Collection<ItemStack> required, Collection<ItemStack> containers) {
        for (ItemStack req : required) {
            if (req == null) {
                // Use an explicit null check here as the collection doesn't have @Nonnull applied to its type
                throw new NullPointerException("Found a null itemstack in " + containers);
            }
            if (req.isEmpty()) continue;
            if (!contains(req, containers)) {
                return false;
            }
        }
        return true;
    }

    public static CompoundTag stripNonFunctionNbt(@Nonnull ItemStack from) {
        CompoundTag nbt = NBTUtilBC.getItemData(from).copy();
        if (nbt.size() == 0) {
            return nbt;
        }
        nbt.remove("_data");
        // TODO: Remove all of the non functional stuff (name, desc, etc)
        return nbt;
    }

    public static boolean doesStackNbtMatch(@Nonnull ItemStack target, @Nonnull ItemStack with) {
        CompoundTag nbtTarget = stripNonFunctionNbt(target);
        CompoundTag nbtWith = stripNonFunctionNbt(with);
        return nbtTarget.equals(nbtWith);
    }

    public static boolean doesEitherStackMatch(@Nonnull ItemStack stackA, @Nonnull ItemStack stackB) {
//        return OreDictionary.itemMatches(stackA, stackB, false) || OreDictionary.itemMatches(stackB, stackA, false);
        return isMatchingItem(stackA, stackB, false, false);
    }

    public static boolean canStacksOrListsMerge(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return false;
        }

        if (stack1.getItem() instanceof IList) {
            IList list = (IList) stack1.getItem();
            return list.matches(stack1, stack2);
        } else if (stack2.getItem() instanceof IList) {
            IList list = (IList) stack2.getItem();
            return list.matches(stack2, stack1);
        }

//        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
        return StackUtil.isSameItemSameDamageSameTag(stack1, stack2);
    }

    /** This doesn't take into account stack sizes.
     *
     * @param filterOrList The exact itemstack to test, or an item that implements {@link IList} to test against.
     * @param test The stack to test for equality
     * @return True if they matched according to the above definitions, or false if theydidn't, or either was empty. */
    public static boolean matchesStackOrList(@Nonnull ItemStack filterOrList, @Nonnull ItemStack test) {
        if (filterOrList.isEmpty() || test.isEmpty()) {
            return false;
        }
        if (filterOrList.getItem() instanceof IList) {
            IList list = (IList) filterOrList.getItem();
            return list.matches(filterOrList, test);
        }
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
        int mergeCount = Math.min(mergeTarget.getMaxStackSize() - mergeTarget.getCount(), mergeSource.getCount());
        if (mergeCount < 1) {
            return 0;
        }
        if (doMerge) {
            mergeTarget.setCount(mergeTarget.getCount() + mergeCount);
        }
        return mergeCount;
    }

    /* ITEM COMPARISONS */

    // TODO Calen: Arrays.stream(oreIDs).anyMatch(comparison::is) may be better?

    /** Determines whether the given ItemStack should be considered equivalent for crafting purposes.
     *
     * @param base The stack to compare to.
     * @param comparison The stack to compare.
     * @param oreDictionary true to take the Forge OreDictionary into account.
     * @return true if comparison should be considered a crafting equivalent for base. */
    public static boolean isCraftingEquivalent(@Nonnull ItemStack base, @Nonnull ItemStack comparison, boolean oreDictionary) {
        if (isMatchingItem(base, comparison, true, false)) {
            return true;
        }

        if (oreDictionary) {
//            int[] idBase = OreDictionary.getOreIDs(base);
            List<TagKey<Item>> idBase = base.getTags().toList();
//            if (idBase.length > 0)
            if (!idBase.isEmpty()) {
//                for (int id : idBase)
                for (TagKey<Item> id : idBase) {
//                    for (ItemStack itemstack : OreDictionary.getOres(OreDictionary.getOreName(id)))
                    for (ItemStack itemstack : ForgeRegistries.ITEMS.tags().getTag(id).stream().map(i -> new ItemStack(i, 1)).toList()) {
                        if (comparison.getItem() == itemstack.getItem()
//                                && (itemstack.getItemDamage() == OreDictionary.WILDCARD_VALUE
                                && (itemstack.getDamageValue() == Short.MAX_VALUE
//                                || comparison.getItemDamage() == itemstack.getItemDamage()))
                                || comparison.getDamageValue() == itemstack.getDamageValue())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // public static boolean isCraftingEquivalent(int[] oreIDs, ItemStack comparison)
    public static boolean isCraftingEquivalent(TagKey<Item>[] oreIDs, ItemStack comparison) {
//        if (oreIDs.length > 0)
        if (oreIDs.length > 0) {
//            for (int id : oreIDs)
            for (TagKey<Item> id : oreIDs) {
//                for (ItemStack itemstack : OreDictionary.getOres(OreDictionary.getOreName(id)))
                for (ItemStack itemstack : ForgeRegistries.ITEMS.tags().getTag(id).stream().map(i -> new ItemStack(i, 1)).toList()) {
                    if (comparison.getItem() == itemstack.getItem()
//                            && (itemstack.getItemDamage() == OreDictionary.WILDCARD_VALUE
                            && (itemstack.getDamageValue() == Short.MAX_VALUE
//                            || comparison.getItemDamage() == itemstack.getItemDamage()))
                            || comparison.getDamageValue() == itemstack.getDamageValue())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isMatchingItemOrList(final ItemStack base, final ItemStack comparison) {
        if (base.isEmpty() || comparison.isEmpty()) {
            return false;
        }

        if (base.getItem() instanceof IList) {
            IList list = (IList) base.getItem();
            return list.matches(base, comparison);
        } else if (comparison.getItem() instanceof IList) {
            IList list = (IList) comparison.getItem();
            return list.matches(comparison, base);
        }

        return isMatchingItem(base, comparison, true, false);
    }

    /** Compares item id, damage and NBT. Accepts wildcard damage. Ignores damage entirely if the item doesn't have
     * subtypes.
     *
     * @param base The stack to compare to.
     * @param comparison The stack to compare.
     * @return true if id, damage and NBT match. */
    public static boolean isMatchingItem(final @Nonnull ItemStack base, final @Nonnull ItemStack comparison) {
        return isMatchingItem(base, comparison, true, true);
    }

    /** This variant also checks damage for damaged items. */
    public static boolean isEqualItem(final @Nonnull ItemStack base, final @Nonnull ItemStack comparison) {
        if (isMatchingItem(base, comparison, false, true)) {
            return isWildcard(base) || isWildcard(comparison) || base.getDamageValue() == comparison.getDamageValue();
        } else {
            return false;
        }
    }

    /** Compares item id, and optionally damage and NBT. Accepts wildcard damage. Ignores damage entirely if the item
     * doesn't have subtypes.
     *
     * @param base ItemStack
     * @param comparison ItemStack
     * @param matchDamage
     * @param matchNBT
     * @return true if matches */
    public static boolean isMatchingItem(@Nonnull final ItemStack base, @Nonnull final ItemStack comparison, final boolean matchDamage, final boolean matchNBT) {
        if (base.isEmpty() || comparison.isEmpty()) {
            return false;
        }

        if (base.getItem() != comparison.getItem()) {
            return false;
        }
//        if (matchDamage && base.getHasSubtypes()) {
//            if (!isWildcard(base) && !isWildcard(comparison)) {
//                if (base.getItemDamage() != comparison.getItemDamage()) {
//                    return false;
//                }
//            }
//        }
        if (matchNBT) {
            CompoundTag baseTag = base.getTag();
            if (baseTag != null && !baseTag.equals(comparison.getTag())) {
                return false;
            }
        } else {
            List<StackMatchingPredicate> predicates = matchingPredicates.getOrDefault(base.getItem(), Collections.emptyList());
            for (StackMatchingPredicate predicate : predicates) {
                if (!predicate.isMatching(base, comparison)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Registers a predicate, that will be used in {@link #isMatchingItem} as an additional comparison rule.
     * If any of registered predicates will return false, then the function will also return false.
     * It can be helpful, if item stacks are clearly not the same, but have common {@link Item} instance.
     * @param forItem {@link Item} instance for which to register the rule.
     * @param predicate predicate to register.
     */
    public static void registerMatchingPredicate(@Nonnull Item forItem, @Nonnull StackMatchingPredicate predicate) {
        List<StackMatchingPredicate> predicates = matchingPredicates.computeIfAbsent(forItem, (item) -> new ArrayList<>());
        predicates.add(predicate);
    }

    /** Checks to see if the given {@link ItemStack} is considered to be a wildcard stack - that is any damage value on
     * the stack will be considered the same as this for recipe purposes.
     *
     * @param stack The stack to check
     * @return True if the stack is a wildcard, false if not. */
    public static boolean isWildcard(@Nonnull ItemStack stack) {
        return isWildcard(stack.getDamageValue());
    }

    /** Checks to see if the given {@link ItemStack} is considered to be a wildcard stack - that is any damage value on
     * the stack will be considered the same as this for recipe purposes.
     *
     * @param damage The damage to check
     * @return True if the damage does specify a wildcard, false if not. */
    public static boolean isWildcard(int damage) {
//        return damage == -1 || damage == OreDictionary.WILDCARD_VALUE;
        return damage == -1 || damage == Short.MAX_VALUE;
    }

    /** @return An empty, nonnull list that cannot be modified (as it cannot be expanded and it has a size of 0) */
    public static NonNullList<ItemStack> listOf() {
        return NonNullList.withSize(0, EMPTY);
    }

    /** Creates a {@link NonNullList} of {@link ItemStack}'s with the elements given in the order that they are given.
     *
     * @param stacks The stacks to put into a list
     * @return A {@link NonNullList} of all the given items. Note that the returned list of of a specified size, and
     *         cannot be expanded. */
    public static NonNullList<ItemStack> listOf(ItemStack... stacks) {
        switch (stacks.length) {
            case 0:
                return listOf();
            case 1:
                return NonNullList.withSize(1, stacks[0]);
            default:
        }
        NonNullList<ItemStack> list = NonNullList.withSize(stacks.length, EMPTY);
        for (int i = 0; i < stacks.length; i++) {
            list.set(i, stacks[i]);
        }
        return list;
    }

    /** Takes a {@link Nullable} {@link Object} and checks to make sure that it is really {@link Nonnull}, like it is
     * everywhere else in the codebase. This is only required if some classes do not use the {@link Nonnull} annotation
     * on return values.
     *
     * @param obj The (potentially) null object.
     * @return A {@link Nonnull} object, which will be the input object
     * @throws NullPointerException if the input object was actually null (Although this should never happen, this is
     *             more to catch bugs in dev.) */
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

    /** @return A {@link Collector} that will collect the input elements into a {@link NonNullList} */
    public static <E> Collector<E, ?, NonNullList<E>> nonNullListCollector() {
        return Collectors.toCollection(NonNullList::create);
    }

    /** Computes a hash code for the given {@link ItemStack}. This is based off of {@link ItemStack#serializeNBT()},
     * except if {@link ItemStack#isEmpty()} returns true, in which case the hash will be 0. */
    public static int hash(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (!stack.hasTag()) {
//            return Objects.hash(stack.getItem(), stack.getMetadata());
            return Objects.hash(stack.getItem(), stack.getDamageValue());
        }
        return stack.serializeNBT().hashCode();
    }

    public static NonNullList<ItemStack> mergeSameItems(List<ItemStack> items) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        for (ItemStack toAdd : items) {
            boolean found = false;
            for (ItemStack stack : stacks) {
                if (canMerge(stack, toAdd)) {
                    stack.grow(toAdd.getCount());
                    found = true;
                }
            }
            if (!found) {
                stacks.add(toAdd.copy());
            }
        }
        return stacks;
    }

    // Calen
    public static boolean isSameItemSameDamage(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        return !stack2.isEmpty() && stack1.getItem() == stack2.getItem() && stack1.getDamageValue() == stack2.getDamageValue();
    }

    public static boolean isSameItemSameDamageSameTag(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        // damage is a tag value
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }

    public static boolean isSameTag(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        return stack1.isEmpty() && stack2.isEmpty() ? true : Objects.equals(stack1.getTag(), stack2.getTag()) && stack1.areCapsCompatible(stack2);
    }
}
