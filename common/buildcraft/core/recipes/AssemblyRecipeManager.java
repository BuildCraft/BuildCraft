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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftCore;
import buildcraft.api.recipes.IAssemblyRecipeManager;
import buildcraft.api.recipes.IFlexibleRecipe;

public class AssemblyRecipeManager implements IAssemblyRecipeManager {

	public static final AssemblyRecipeManager INSTANCE = new AssemblyRecipeManager();
	private Map<String, IFlexibleRecipe> assemblyRecipes = new HashMap<String, IFlexibleRecipe>();

	@Override
	public void addRecipe(String id, double energyCost, ItemStack output, Object... input) {
		String name = Item.itemRegistry.getNameForObject(output.getItem());

		if (BuildCraftCore.recipesBlacklist.contains(name)) {
			return;
		}

		addRecipe(id, new FlexibleRecipe(id, output, energyCost, input));
	}

	@Override
	public void addRecipe(IFlexibleRecipe recipe) {
		addRecipe(recipe.getId(), recipe);
	}

	private void addRecipe(String id, IFlexibleRecipe recipe) {
		if (assemblyRecipes.containsKey(id)) {
			throw new RuntimeException("Recipe \"" + id + "\" already registered");
		}

		assemblyRecipes.put(recipe.getId(), recipe);
	}

	@Override
	public Collection<IFlexibleRecipe> getRecipes() {
		return assemblyRecipes.values();
	}

	public IFlexibleRecipe getRecipe(String id) {
		return assemblyRecipes.get(id);
	}
}
