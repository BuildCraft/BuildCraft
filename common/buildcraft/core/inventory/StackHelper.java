/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class StackHelper {

	private static StackHelper instance;

	public static StackHelper instance() {
		if (instance == null) {
			instance = new StackHelper();
		}
		return instance;
	}

	public static void setInstance(StackHelper inst) {
		instance = inst;
	}

	protected StackHelper() {
	}

	/* STACK MERGING */
	/**
	 * Checks if two ItemStacks are identical enough to be merged
	 *
	 * @param stack1 - The first stack
	 * @param stack2 - The second stack
	 * @return true if stacks can be merged, false otherwise
	 */
	public boolean canStacksMerge(ItemStack stack1, ItemStack stack2) {
		if (stack1 == null || stack2 == null)
			return false;
		if (!stack1.isItemEqual(stack2))
			return false;
		if (!ItemStack.areItemStackTagsEqual(stack1, stack2))
			return false;
		return true;

	}

	/**
	 * Merges mergeSource into mergeTarget
	 *
	 * @param mergeSource - The stack to merge into mergeTarget, this stack is
	 * not modified
	 * @param mergeTarget - The target merge, this stack is modified if doMerge
	 * is set
	 * @param doMerge - To actually do the merge
	 * @return The number of items that was successfully merged.
	 */
	public int mergeStacks(ItemStack mergeSource, ItemStack mergeTarget, boolean doMerge) {
		if (!canStacksMerge(mergeSource, mergeTarget))
			return 0;
		int mergeCount = Math.min(mergeTarget.getMaxStackSize() - mergeTarget.stackSize, mergeSource.stackSize);
		if (mergeCount < 1)
			return 0;
		if (doMerge) {
			mergeTarget.stackSize += mergeCount;
		}
		return mergeCount;
	}

	/* ITEM COMPARISONS */
	/**
	 * Determines whether the given itemstacks should be considered equivalent
	 * for crafting purposes.
	 *
	 * @param base The stack to compare to.
	 * @param comparison The stack to compare.
	 * @param oreDictionary true to take the Forge OreDictionary into account.
	 * @return true if comparison should be considered a crafting equivalent for
	 * base.
	 */
	public boolean isCraftingEquivalent(ItemStack base, ItemStack comparison, boolean oreDictionary) {
		if (isMatchingItem(base, comparison))
			return true;

		if (oreDictionary) {
			int idBase = OreDictionary.getOreID(base);
			if (idBase >= 0) {
				int idComp = OreDictionary.getOreID(comparison);
				if (idComp >= 0) {
					if (idBase == idComp)
						return true;
				}
			}
		}

		return false;
	}

	/**
	 * Compares item id, damage and NBT. Accepts wildcard damage in the base
	 * ItemStack. Ignores damage entirely if the item doesn't have subtypes.
	 *
	 * @param base The stack to compare to.
	 * @param comparison The stack to compare.
	 * @return true if id, damage and NBT match.
	 */
	public boolean isMatchingItem(ItemStack base, ItemStack comparison) {
		if (base == null || comparison == null)
			return false;

		if (base.itemID != comparison.itemID)
			return false;

		if (base.getItem().getHasSubtypes()) {
			if (base.getItemDamage() != OreDictionary.WILDCARD_VALUE)
				if (base.getItemDamage() != comparison.getItemDamage())
					return false;
		}

		return ItemStack.areItemStackTagsEqual(base, comparison);
	}
}
