/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.recipes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import buildcraft.BuildCraftCore;
import buildcraft.api.recipes.IAssemblyRecipeManager;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.core.utils.Utils;

public class AssemblyRecipeManager implements IAssemblyRecipeManager {

	public static final AssemblyRecipeManager INSTANCE = new AssemblyRecipeManager();
	private Map<String, IFlexibleRecipe<ItemStack>> assemblyRecipes = new HashMap<String, IFlexibleRecipe<ItemStack>>();

	@Override
	public void addRecipe(String id, int energyCost, ItemStack output, Object... input) {
		String name = (String) Utils.getItemName(output.getItem());

		if (BuildCraftCore.recipesBlacklist.contains(name)) {
			return;
		}

		addRecipe(id, new FlexibleRecipe<ItemStack>(id, output, energyCost, 0, input));
	}

	@Override
	public void addRecipe(IFlexibleRecipe<ItemStack> recipe) {
		addRecipe(recipe.getId(), recipe);
	}

	private void addRecipe(String id, IFlexibleRecipe<ItemStack> recipe) {
		if (assemblyRecipes.containsKey(id)) {
			throw new RuntimeException("Recipe \"" + id + "\" already registered");
		}

		assemblyRecipes.put(recipe.getId(), recipe);
	}

	@Override
	public Collection<IFlexibleRecipe<ItemStack>> getRecipes() {
		return assemblyRecipes.values();
	}

	public IFlexibleRecipe<ItemStack> getRecipe(String id) {
		return assemblyRecipes.get(id);
	}

	@Override
	public void removeRecipe(IFlexibleRecipe<ItemStack> recipe) {
		removeRecipe(recipe.getId());
	}

	@Override
	public void removeRecipe(String id) {
		assemblyRecipes.remove(id);
	}
}
