/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.inventory;

import net.minecraft.item.ItemStack;

public class StackMergeHelper {

	/**
	 * Checks if two ItemStacks are identical enough to be merged
	 * @param stack1 - The first stack
	 * @param stack2 - The second stack
	 * @return true if stacks can be merged, false otherwise
	 */
	
	public boolean canStacksMerge(ItemStack stack1, ItemStack stack2){
		if (stack1 == null || stack2 == null) return false;
		if (!stack1.isItemEqual(stack2)) return false;
		if (!ItemStack.areItemStackTagsEqual(stack1, stack2)) return false;
		return true;
		
	}
	
	/**
	 * Merges mergeSource into mergeTarget
	 * @param mergeSource - The stack to merge into mergeTarget, this stack is not modified
	 * @param mergeTarget - The target merge, this stack is modified if doMerge is set
	 * @param doMerge - To actually do the merge
	 * @return The number of items that was successfully merged.
	 */
	public int mergeStacks(ItemStack mergeSource, ItemStack mergeTarget, boolean doMerge){
		if (!canStacksMerge(mergeSource, mergeTarget)) return 0;
		int mergeCount = Math.min(mergeTarget.getMaxStackSize() - mergeTarget.stackSize, mergeSource.stackSize);
		if (mergeCount < 1) return 0;
		if (doMerge){
			mergeTarget.stackSize += mergeCount;
		}
		return mergeCount;
	}
		
}
