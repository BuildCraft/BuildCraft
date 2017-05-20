package buildcraft.lib.recipe;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IAssemblyRecipeProvider;
import buildcraft.api.recipes.IAssemblyRecipeRegistry;

import buildcraft.lib.misc.StackUtil;
import net.minecraft.util.ResourceLocation;

public enum AssemblyRecipeRegistry implements IAssemblyRecipeRegistry {
    INSTANCE;

    private final Map<ResourceLocation, AssemblyRecipe> recipes = new HashMap<>();
    private final List<IAssemblyRecipeProvider> providers = new ArrayList<>();

    @Nonnull
    @Override
    public List<AssemblyRecipe> getRecipesFor(@Nonnull NonNullList<ItemStack> possibleIn) {
        List<AssemblyRecipe> all = new ArrayList<>();
        for (AssemblyRecipe ar : recipes.values()) {
            if (ar.requiredStacks.stream().allMatch((definition) -> StackUtil.contains(definition, possibleIn))) {
                all.add(ar);
            }
        }
        for (IAssemblyRecipeProvider provider : providers) {
            all.addAll(provider.getRecipesFor(possibleIn));
        }
        return all;
    }

    @Override
    public void addRecipe(@Nonnull AssemblyRecipe recipe) {
        if (recipes.containsKey(recipe.name)) {
            throw new IllegalStateException("Trying to override assembly recipe with name " + recipe.name + ".\n" +
                    "If you want replace recipe remove old one first.");
        }
        recipes.put(recipe.name, recipe);
    }

    @Override
    public void addRecipeProvider(@Nonnull IAssemblyRecipeProvider provider) {
        providers.add(provider);
    }

    @Override
    public Iterable<AssemblyRecipe> getAllRecipes() {
        return recipes.values();
    }

    @Override
    public Iterable<IAssemblyRecipeProvider> getAllRecipeProviders() {
        return providers;
    }

    @Override
    public Optional<AssemblyRecipe> getRecipe(@Nonnull ResourceLocation name, @Nullable NBTTagCompound recipeTag) {
        AssemblyRecipe recipe = recipes.get(name);
        if (recipe != null) return Optional.of(recipe);
        return providers.stream().map(provider -> provider.getRecipe(name, recipeTag).orElse(null))
                .filter(Objects::nonNull).findFirst();
    }
}
