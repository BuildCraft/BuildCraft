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
import java.util.Collections;
import java.util.HashMap;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.recipes.IRefineryRecipeManager;

public final class RefineryRecipeManager implements IRefineryRecipeManager {

	public static final RefineryRecipeManager INSTANCE = new RefineryRecipeManager();
	private HashMap<String, IFlexibleRecipe<FluidStack>> recipes = new HashMap<String, IFlexibleRecipe<FluidStack>>();

	private RefineryRecipeManager() {
	}

	@Override
	public void addRecipe(String id, FluidStack ingredient, FluidStack result, int energy, int delay) {
		String name = result.getFluid().getName();

		if (BuildCraftCore.recipesBlacklist.contains(name)) {
			return;
		}


		FlexibleRecipe<FluidStack> recipe = new FlexibleRecipe<FluidStack>(id, result, energy, delay, ingredient);
		recipes.put(id, recipe);
	}

	@Override
	public void addRecipe(String id, FluidStack ingredient1, FluidStack ingredient2, FluidStack result, int energy,
			int delay) {
		String name = result.getFluid().getName();

		if (BuildCraftCore.recipesBlacklist.contains(name)) {
			return;
		}
		
		if (ingredient1 == null || ingredient2 == null || result == null) {
			BCLog.logger.warn("Rejected refinery recipe " + id + " due to a null FluidStack!");
		}

		FlexibleRecipe<FluidStack> recipe = new FlexibleRecipe<FluidStack>(id, result, energy, delay, ingredient1,
				ingredient2);
		recipes.put(id, recipe);
	}

	@Override
	public Collection<IFlexibleRecipe<FluidStack>> getRecipes() {
		return Collections.unmodifiableCollection(recipes.values());
	}

	@Override
	public IFlexibleRecipe<FluidStack> getRecipe(String id) {
		return recipes.get(id);
	}
	
	@Override
	public void removeRecipe(IFlexibleRecipe<FluidStack> recipe) {
		removeRecipe(recipe.getId());
	}

	@Override
	public void removeRecipe(String id) {
		recipes.remove(id);
	}
}
