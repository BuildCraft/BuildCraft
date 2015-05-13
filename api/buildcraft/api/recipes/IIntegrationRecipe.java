/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.recipes;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IIntegrationRecipe {
	int getEnergyCost();
	List<ItemStack> getExampleInput();
	List<List<ItemStack>> getExampleExpansions();
	List<ItemStack> getExampleOutput();
	boolean isValidInput(ItemStack input);
	boolean isValidExpansion(ItemStack input, ItemStack expansion);
	ItemStack craft(ItemStack input, List<ItemStack> expansions, boolean preview);

	/**
	 * @return -1 for no limit, a different number otherwise
	 */
	int getMaximumExpansionCount(ItemStack input);
}