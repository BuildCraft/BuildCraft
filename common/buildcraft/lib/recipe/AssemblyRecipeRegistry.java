package buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IAssemblyRecipeProvider;
import buildcraft.api.recipes.IAssemblyRecipeRegistry;

import buildcraft.lib.misc.StackUtil;

public enum AssemblyRecipeRegistry implements IAssemblyRecipeRegistry {
    INSTANCE;

    private final List<AssemblyRecipe> recipes = new ArrayList<>();
    private final List<IAssemblyRecipeProvider> providers = new ArrayList<>();

    @Override
    public List<AssemblyRecipe> getRecipesFor(NonNullList<ItemStack> possibleIn) {
        List<AssemblyRecipe> all = new ArrayList<>();
        for (AssemblyRecipe ar : recipes) {
            if (StackUtil.containsAll(ar.requiredStacks, possibleIn)) {
                all.add(ar);
            }
        }
        for (IAssemblyRecipeProvider provider : providers) {
            all.addAll(provider.getRecipesFor(possibleIn));
        }
        return all;
    }

    @Override
    public void addRecipe(AssemblyRecipe recipe) {
        recipes.add(recipe);
    }

    @Override
    public void addRecipeProvider(IAssemblyRecipeProvider provider) {
        providers.add(provider);
    }

    @Override
    public Iterable<AssemblyRecipe> getAllRecipes() {
        return recipes;
    }

    @Override
    public Iterable<IAssemblyRecipeProvider> getAllRecipeProviders() {
        return providers;
    }
}
