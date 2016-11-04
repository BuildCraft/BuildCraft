/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.item.ItemStack;

import buildcraft.lib.misc.StackUtil;

@Deprecated
public class StackHelper {

    protected StackHelper() {}

    /* STACK MERGING */

    /**
     * @deprecated Use {@link StackUtil#canStacksOrListsMerge(ItemStack,ItemStack)} instead
     */
    public static boolean canStacksOrListsMerge(ItemStack stack1, ItemStack stack2) {
        return StackUtil.canStacksOrListsMerge(stack1, stack2);
    }

    /** Merges mergeSource into mergeTarget
     *
     * @param mergeSource - The stack to merge into mergeTarget, this stack is not modified
     * @param mergeTarget - The target merge, this stack is modified if doMerge is set
     * @param doMerge - To actually do the merge
     * @return The number of items that was successfully merged. 
     * @deprecated Use {@link StackUtil#mergeStacks(ItemStack,ItemStack,boolean)} instead*/
    public static int mergeStacks(ItemStack mergeSource, ItemStack mergeTarget, boolean doMerge) {
        return StackUtil.mergeStacks(mergeSource, mergeTarget, doMerge);
    }

    /* ITEM COMPARISONS */
    /** Determines whether the given ItemStack should be considered equivalent for crafting purposes.
     *
     * @param base The stack to compare to.
     * @param comparison The stack to compare.
     * @param oreDictionary true to take the Forge OreDictionary into account.
     * @return true if comparison should be considered a crafting equivalent for base. 
     * @deprecated Use {@link StackUtil#isCraftingEquivalent(ItemStack,ItemStack,boolean)} instead*/
    public static boolean isCraftingEquivalent(ItemStack base, ItemStack comparison, boolean oreDictionary) {
        return StackUtil.isCraftingEquivalent(base, comparison, oreDictionary);
    }

    /**
     * @deprecated Use {@link StackUtil#isCraftingEquivalent(int[],ItemStack)} instead
     */
    public static boolean isCraftingEquivalent(int[] oreIDs, ItemStack comparison) {
        return StackUtil.isCraftingEquivalent(oreIDs, comparison);
    }

    /**
     * @deprecated Use {@link StackUtil#isMatchingItemOrList(ItemStack,ItemStack)} instead
     */
    public static boolean isMatchingItemOrList(final ItemStack a, final ItemStack b) {
        return StackUtil.isMatchingItemOrList(a, b);
    }

    /** Compares item id, damage and NBT. Accepts wildcard damage. Ignores damage entirely if the item doesn't have
     * subtypes.
     *
     * @param base The stack to compare to.
     * @param comparison The stack to compare.
     * @return true if id, damage and NBT match. 
     * @deprecated Use {@link StackUtil#isMatchingItem(ItemStack,ItemStack)} instead*/
    public static boolean isMatchingItem(final ItemStack base, final ItemStack comparison) {
        return StackUtil.isMatchingItem(base, comparison);
    }

    /** This variant also checks damage for damaged items. 
     * @deprecated Use {@link StackUtil#isEqualItem(ItemStack,ItemStack)} instead*/
    public static boolean isEqualItem(final ItemStack a, final ItemStack b) {
        return StackUtil.isEqualItem(a, b);
    }

    /** Compares item id, and optionally damage and NBT. Accepts wildcard damage. Ignores damage entirely if the item
     * doesn't have subtypes.
     *
     * @param a ItemStack
     * @param b ItemStack
     * @param matchDamage
     * @param matchNBT
     * @return true if matches 
     * @deprecated Use {@link StackUtil#isMatchingItem(ItemStack,ItemStack,boolean,boolean)} instead*/
    public static boolean isMatchingItem(final ItemStack a, final ItemStack b, final boolean matchDamage, final boolean matchNBT) {
        return StackUtil.isMatchingItem(a, b, matchDamage, matchNBT);
    }

    /**
     * @deprecated Use {@link StackUtil#isWildcard(ItemStack)} instead
     */
    public static boolean isWildcard(ItemStack stack) {
        return StackUtil.isWildcard(stack);
    }

    /**
     * @deprecated Use {@link StackUtil#isWildcard(int)} instead
     */
    public static boolean isWildcard(int damage) {
        return StackUtil.isWildcard(damage);
    }
}
