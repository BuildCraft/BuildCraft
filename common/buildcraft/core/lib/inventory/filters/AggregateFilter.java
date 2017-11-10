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

/**
 * Returns true if the stack matches all of the stack filters.
 */
public class AggregateFilter implements IStackFilter {

	private final IStackFilter[] filters;

	public AggregateFilter(IStackFilter... iFilters) {
		filters = iFilters;
	}

	@Override
	public boolean matches(ItemStack stack) {
		for (IStackFilter f : filters) {
			if (!f.matches(stack)) {
				return false;
			}
		}

		return true;
	}
}
