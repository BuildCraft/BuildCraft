/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.inventory.filters;

import net.minecraft.item.ItemStack;

import buildcraft.core.lib.inventory.StackHelper;

/**
 * Returns true if the stack matches any one one of the filter stacks. Takes
 * into account item lists.
 */
public class ArrayStackOrListFilter extends ArrayStackFilter {

	public ArrayStackOrListFilter(ItemStack... stacks) {
		super(stacks);
	}

	@Override
	public boolean matches(ItemStack stack) {
		if (stacks.length == 0 || !hasFilter()) {
			return true;
		}

		for (ItemStack s : stacks) {
			if (StackHelper.isMatchingItemOrList(s, stack)) {
				return true;
			}
		}

		return false;
	}
}
