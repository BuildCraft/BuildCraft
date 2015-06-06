package buildcraft.core.recipes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import buildcraft.api.core.BCLog;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.api.recipes.IProgrammingRecipeManager;

public class ProgrammingRecipeManager implements IProgrammingRecipeManager {
	public static final ProgrammingRecipeManager INSTANCE = new ProgrammingRecipeManager();
	private final HashMap<String, IProgrammingRecipe> recipes = new HashMap<String, IProgrammingRecipe>();

	@Override
	public void addRecipe(IProgrammingRecipe recipe) {
		if (recipe == null || recipe.getId() == null) {
			return;
		}

		if (!recipes.containsKey(recipe.getId())) {
			recipes.put(recipe.getId(), recipe);
		} else {
			BCLog.logger.warn("Programming Table Recipe '" + recipe.getId() + "' seems to be duplicated! This is a bug!");
		}
	}

	@Override
	public void removeRecipe(String id) {
		recipes.remove(id);
	}

	@Override
	public void removeRecipe(IProgrammingRecipe recipe) {
		if (recipe == null || recipe.getId() == null) {
			return;
		}

		recipes.remove(recipe.getId());
	}

	@Override
	public Collection<IProgrammingRecipe> getRecipes() {
		return Collections.unmodifiableCollection(recipes.values());
	}

	@Override
	public IProgrammingRecipe getRecipe(String id) {
		return recipes.get(id);
	}
}
