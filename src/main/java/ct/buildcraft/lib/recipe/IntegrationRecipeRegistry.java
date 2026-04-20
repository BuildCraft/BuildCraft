/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.recipe;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import ct.buildcraft.api.recipes.IIntegrationRecipeRegistry;
import ct.buildcraft.api.recipes.IntegrationRecipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class IntegrationRecipeRegistry implements IIntegrationRecipeRegistry {
    public static final IntegrationRecipeRegistry INSTANCE = new IntegrationRecipeRegistry();
    public final Map<ResourceLocation, IntegrationRecipe> recipes = new HashMap<>();

    @Override
    public IntegrationRecipe getRecipeFor(@Nonnull ItemStack target, @Nonnull NonNullList<ItemStack> toIntegrate) {
        for (IntegrationRecipe recipe : recipes.values()) {
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
    public Iterable<IntegrationRecipe> getAllRecipes() {
        return recipes.values();
    }


    @Override
    public IntegrationRecipe getRecipe(@Nonnull ResourceLocation name) {
        return recipes.get(name);
    }
}
