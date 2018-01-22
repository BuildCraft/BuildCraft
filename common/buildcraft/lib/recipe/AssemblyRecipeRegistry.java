/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IAssemblyRecipeProvider;
import buildcraft.api.recipes.IAssemblyRecipeRegistry;

import buildcraft.lib.misc.StackUtil;

public enum AssemblyRecipeRegistry implements IAssemblyRecipeRegistry {
    INSTANCE;

    private final Map<ResourceLocation, AssemblyRecipe> recipes = new HashMap<>();
    private final List<IAssemblyRecipeProvider> providers = new ArrayList<>();

    @Nonnull
    @Override
    public List<AssemblyRecipe> getRecipesFor(@Nonnull List<ItemStack> possibleIn) {
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
