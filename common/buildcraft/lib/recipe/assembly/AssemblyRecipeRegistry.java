/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe.assembly;

import buildcraft.api.recipes.IAssemblyRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AssemblyRecipeRegistry {
    public static IFacadeAssemblyRecipes FACADE_ASSEMBLY_RECIPE;

    @Deprecated
    // public static final Map<ResourceLocation, AssemblyRecipe> REGISTRY = new HashMap<>();
    private static final List<IAssemblyRecipe> REGISTRY = new LinkedList<>();

    public static List<IAssemblyRecipe> getAll(World world) {
        List<IAssemblyRecipe> ret = world.getRecipeManager().getAllRecipesFor(IAssemblyRecipe.TYPE);
        ret.addAll(REGISTRY);
        return ret;
    }

    /** Don't call this, and use datagen instead! */
    @Deprecated
    public static void register(IAssemblyRecipe recipe) {
//        REGISTRY.put(recipe.getRegistryName(), recipe);
        REGISTRY.add(recipe);
    }

    // Calen: never used in 1.12.2
    @Nonnull
//    public static List<AssemblyRecipe> getRecipesFor(@Nonnull NonNullList<ItemStack> possibleIn)
    public static List<IAssemblyRecipe> getRecipesFor(World world, @Nonnull NonNullList<ItemStack> possibleIn) {
        List<IAssemblyRecipe> all = new ArrayList<>();
//        for (AssemblyRecipe ar : REGISTRY.values())
        for (IAssemblyRecipe ar : getAll(world)) {
            if (!ar.getOutputs(possibleIn).isEmpty()) {
                all.add(ar);
            }
        }
        return all;
    }
}
