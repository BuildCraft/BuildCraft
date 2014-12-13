/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.recipes;

import java.util.List;

/**
 * The Integration Table's primary purpose is to modify an input item's NBT
 * data. As such its not a "traditional" type of recipe. Rather than predefined
 * inputs and outputs, it takes an input and transforms it.
 */
public interface IIntegrationRecipeManager {
	/**
	 * Add an Integration Table recipe.
	 */
	void addRecipe(IIntegrationRecipe recipe);

	List<? extends IIntegrationRecipe> getRecipes();
}
