/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.recipes;

import java.util.LinkedList;
import java.util.List;

import buildcraft.api.recipes.IIntegrationRecipe;
import buildcraft.api.recipes.IIntegrationRecipeManager;

public class IntegrationRecipeManager implements IIntegrationRecipeManager {
	public static final IntegrationRecipeManager INSTANCE = new IntegrationRecipeManager();
	private List<IIntegrationRecipe> integrationRecipes = new LinkedList<IIntegrationRecipe>();

	@Override
	public void addRecipe(IIntegrationRecipe recipe) {
		integrationRecipes.add(recipe);
	}

	@Override
	public List<? extends IIntegrationRecipe> getRecipes() {
		return integrationRecipes;
	}
}
