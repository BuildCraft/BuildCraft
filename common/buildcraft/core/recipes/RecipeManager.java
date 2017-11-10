/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.recipes;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.recipes.IRecipeManager;

public class RecipeManager<T> implements IRecipeManager<T> {
	private BiMap<String, IFlexibleRecipe<T>> recipes = HashBiMap.create();

	@Override
	public void addRecipe(String id, int energyCost, T output, Object... input) {
		addRecipe(id, energyCost, 0, output, input);
	}

	@Override
	public void addRecipe(String id, int energyCost, int craftingDelay, T output, Object... input) {
		recipes.put(id, new FlexibleRecipe<T>(id, output, energyCost, craftingDelay, input));
	}

	@Override
	public void addRecipe(IFlexibleRecipe<T> recipe) {
		recipes.put(recipe.getId(), recipe);
	}

	@Override
	public void removeRecipe(String id) {
		recipes.remove(id);
	}

	@Override
	public void removeRecipe(IFlexibleRecipe<T> recipe) {
		recipes.remove(recipes.inverse().get(recipe));
	}

	@Override
	public Collection<IFlexibleRecipe<T>> getRecipes() {
		return Collections.unmodifiableCollection(recipes.values());
	}

	@Override
	public IFlexibleRecipe<T> getRecipe(String id) {
		return recipes.get(id);
	}
}
