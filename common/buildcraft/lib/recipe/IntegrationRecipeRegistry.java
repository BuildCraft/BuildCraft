package buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.api.recipes.IIntegrationRecipeProvider;
import buildcraft.api.recipes.IIntegrationRecipeRegistry;
import buildcraft.api.recipes.IntegrationRecipe;

import buildcraft.lib.misc.StackUtil;

public enum IntegrationRecipeRegistry implements IIntegrationRecipeRegistry {
    INSTANCE;
    private final List<IntegrationRecipe> recipes = new ArrayList<>();
    private final List<IIntegrationRecipeProvider> providers = new ArrayList<>();

    @Override
    public IntegrationRecipe getRecipeFor(ItemStack target, List<ItemStack> toIntegrate) {
        for (IntegrationRecipe recipe : recipes) {
            if (matches(recipe, target, toIntegrate)) {
                return recipe;
            }
        }
        for (IIntegrationRecipeProvider provider : providers) {
            IntegrationRecipe possible = provider.getRecipeFor(target, toIntegrate);
            if (possible != null) {
                return possible;
            }
        }
        return null;
    }

    public static boolean matches(IntegrationRecipe recipe, ItemStack target, List<ItemStack> toIntegrate) {
        if (!StackUtil.contains(recipe.target, target)) {
            return false;
        }
        if (recipe.toIntegrate.size() != toIntegrate.size()) {
            return false;
        }
        for (int i = 0; i < toIntegrate.size(); i++) {
            ItemStack required = recipe.toIntegrate.get(i);
            ItemStack given = toIntegrate.get(i);
            if (!StackUtil.contains(required, given)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addRecipe(IntegrationRecipe recipe) {
        recipes.add(recipe);
    }

    @Override
    public void addRecipeProvider(IIntegrationRecipeProvider provider) {
        providers.add(provider);
    }

    @Override
    public Iterable<IntegrationRecipe> getAllRecipes() {
        return recipes;
    }

    @Override
    public Iterable<IIntegrationRecipeProvider> getAllRecipeProviders() {
        return providers;
    }
}
