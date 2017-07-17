/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import buildcraft.api.recipes.AssemblyRecipe;

public class AssemblyRecipeRegistry  {
    public static final IForgeRegistry<AssemblyRecipe> REGISTRY;

    static {
        REGISTRY = new RegistryBuilder().disableSaving().setType(AssemblyRecipe.class).setName(new ResourceLocation("buildcraftlib:AssemblyRecipeRegistry")).create();
    }

    @Nonnull
    public static List<AssemblyRecipe> getRecipesFor(@Nonnull NonNullList<ItemStack> possibleIn) {
        List<AssemblyRecipe> all = new ArrayList<>();
        for (AssemblyRecipe ar : REGISTRY.getValues()) {
            if (!ar.getOutputs(possibleIn).isEmpty()) {
                all.add(ar);
            }
        }
        return all;
    }
}
