/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;

public enum GuideCraftingRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack target) {
        List<GuidePartFactory> list = new ArrayList<>();

        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (checkRecipeUses(recipe, target)) {
                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    private static boolean checkRecipeUses(IRecipe recipe, @Nonnull ItemStack target) {
        NonNullList<Ingredient> ingrediants = recipe.getIngredients();
        if (ingrediants.isEmpty()) {
            if (recipe instanceof IRecipeViewable) {
                // TODO!
            }
        }
        for (Ingredient ing : ingrediants) {
            if (ing.test(target)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(@Nonnull ItemStack target, @Nullable Object in) {
        if (in instanceof ItemStack) {
            return StackUtil.doesEitherStackMatch((ItemStack) in, target);
        } else if (in instanceof List) {
            for (Object obj : (List<?>) in) {
                if (obj instanceof ItemStack) {
                    if (StackUtil.doesEitherStackMatch((ItemStack) obj, target)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack target) {
        List<GuidePartFactory> list = new ArrayList<>();

        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (recipe instanceof IRecipeViewable) {
                ChangingItemStack changing = ((IRecipeViewable) recipe).getRecipeOutputs();
                if (changing.matches(target)) {
                    GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                    if (factory != null) {
                        list.add(factory);
                    }
                }
            } else {
                ItemStack out = StackUtil.asNonNull(recipe.getRecipeOutput());
                if (OreDictionary.itemMatches(target, out, false) || OreDictionary.itemMatches(out, target, false)) {
                    GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                    if (factory != null) {
                        list.add(factory);
                    }
                }
            }
        }

        return list;
    }
}
