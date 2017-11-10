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

import net.minecraftforge.oredict.OreDictionary;

/**
 * Returns true if the stack matches any one one of the filter stacks.
 */
public class OreStackFilter implements IStackFilter {

	private final String[] ores;

	public OreStackFilter(String... iOres) {
		ores = iOres;
	}

	@Override
	public boolean matches(ItemStack stack) {
		int[] ids = OreDictionary.getOreIDs(stack);

		if (ids.length == 0) {
			return false;
		}

		for (String ore : ores) {
			int expected = OreDictionary.getOreID(ore);

			for (int id : ids) {
				if (id == expected) {
					return true;
				}
			}
		}

		return false;
	}
}
