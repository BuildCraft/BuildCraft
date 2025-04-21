/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe.integration;

import buildcraft.api.recipes.IIntegrationRecipeRegistry;
import buildcraft.api.recipes.IntegrationRecipe;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IntegrationRecipeRegistry implements IIntegrationRecipeRegistry {
    public static final IntegrationRecipeRegistry INSTANCE = new IntegrationRecipeRegistry();
    public final Map<ResourceLocation, IntegrationRecipe> recipes = new HashMap<>();

    @Override
    // public IntegrationRecipe getRecipeFor(@Nonnull ItemStack target, @Nonnull NonNullList<ItemStack> toIntegrate)
    public IntegrationRecipe getRecipeFor(@Nonnull ItemStack target, @Nonnull NonNullList<ItemStack> toIntegrate, Level world) {
        // for (IntegrationRecipe recipe : recipes.values())
        for (IntegrationRecipe recipe : getAllRecipes(world)) {
            if (!recipe.getOutput(target, toIntegrate).isEmpty()) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public void addRecipe(IntegrationRecipe recipe) {
        if (recipes.containsKey(recipe.name)) {
            throw new IllegalStateException("Trying to override integration recipe with name " + recipe.name + ".\n" +
                    "If you want replace recipe remove old one first.");
        }
        recipes.put(recipe.name, recipe);
    }

    @Override
    // public Iterable<IntegrationRecipe> getAllRecipes()
    public Iterable<IntegrationRecipe> getAllRecipes(Level world) {
        List<IntegrationRecipe> ret = Lists.newArrayList();
        ret.addAll(world.getRecipeManager().getAllRecipesFor(IntegrationRecipe.TYPE));
        ret.addAll(recipes.values());
        return ret;
    }


    @Override
    // public IntegrationRecipe getRecipe(@Nonnull ResourceLocation name)
    public IntegrationRecipe getRecipe(@Nonnull ResourceLocation name, Level world) {
        List<IntegrationRecipe> all = world.getRecipeManager().getAllRecipesFor(IntegrationRecipe.TYPE);
        List<IntegrationRecipe> found = all.stream().filter(r -> name.equals(r.name)).collect(Collectors.toList());
        if (!found.isEmpty()) {
            return found.get(0);
        } else {
            return recipes.get(name);
        }
    }
}
