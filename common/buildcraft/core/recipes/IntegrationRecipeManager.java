package buildcraft.core.recipes;

import buildcraft.api.recipes.IIntegrationRecipeManager;
import buildcraft.api.recipes.IIntegrationRecipeManager.IIntegrationRecipe;
import java.util.LinkedList;
import java.util.List;

public class IntegrationRecipeManager implements IIntegrationRecipeManager {

	public static final IntegrationRecipeManager INSTANCE = new IntegrationRecipeManager();
	private List<IIntegrationRecipe> integrationRecipes = new LinkedList<IIntegrationRecipe>();

	@Override
	public void addRecipe(IIntegrationRecipe recipe) {
		integrationRecipes.add(recipe);
	}

	@Override
	public List<IIntegrationRecipe> getRecipes() {
		return integrationRecipes;
	}
}
