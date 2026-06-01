/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import buildcraft.lib.compat.forge_stubs.OreDictionaryStub;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
// STUB(R.Chen): OreDictionaryStub removed — TODO(R.Chen): implement via Tags

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;

public enum GuideCraftingRecipes implements IStackRecipes {
    INSTANCE;

    private static final boolean USE_INDEX = true;

    private Map<Item, Set<net.minecraft.recipe.CraftingRecipe>> inputIndexMap, outputIndexMap;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack target) {
        final Iterable<net.minecraft.recipe.CraftingRecipe> recipes;
        if (USE_INDEX) {
            generateInputIndex();
            recipes = inputIndexMap.get(target.getItem());
            if (recipes == null) {
                return ImmutableList.of();
            }
        } else {
            recipes = ForgeRegistries.RECIPES;
        }

        List<GuidePartFactory> list = new ArrayList<>();
        for (net.minecraft.recipe.CraftingRecipe recipe : recipes) {
            if (checkRecipeUses(recipe, target)) {
                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    public void generateIndices() {
        if (USE_INDEX) {
            generateInputIndex();
            generateOutputIndex();
        }
    }

    private void generateInputIndex() {
        if (inputIndexMap == null) {
            inputIndexMap = new IdentityHashMap<>();
            for (net.minecraft.recipe.CraftingRecipe recipe : ForgeRegistries.RECIPES) {
                generateInputIndex0(recipe);
            }
        }
    }

    private void generateInputIndex0(net.minecraft.recipe.CraftingRecipe recipe) {
        for (Ingredient ing : recipe.getIngredients()) {
            generateIngredientIndex(recipe, ing, inputIndexMap);
        }
    }

    private static void generateIngredientIndex(net.minecraft.recipe.CraftingRecipe recipe, Ingredient ing, Map<Item, Set<net.minecraft.recipe.CraftingRecipe>> indexMap) {
        for (ItemStack stack : ing.getMatchingStacks()) {
            appendIndex(stack, recipe, indexMap);
        }
    }

    private static void appendIndex(ItemStack stack, net.minecraft.recipe.CraftingRecipe recipe, Map<Item, Set<net.minecraft.recipe.CraftingRecipe>> indexMap) {
        Set<net.minecraft.recipe.CraftingRecipe> list = indexMap.get(stack.getItem());
        if (list == null) {
            list = new LinkedHashSet<>();
            indexMap.put(stack.getItem(), list);
        }
        list.add(recipe);
    }

    private static boolean checkRecipeUses(net.minecraft.recipe.CraftingRecipe recipe, @Nonnull ItemStack target) {
        DefaultedList<Ingredient> ingrediants = recipe.getIngredients();
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
        final Iterable<net.minecraft.recipe.CraftingRecipe> recipes;
        if (USE_INDEX) {
            generateOutputIndex();
            recipes = outputIndexMap.get(target.getItem());
            if (recipes == null) {
                return ImmutableList.of();
            }

        } else {
            recipes = ForgeRegistries.RECIPES;
        }

        List<GuidePartFactory> list = new ArrayList<>();
        for (net.minecraft.recipe.CraftingRecipe recipe : recipes) {
            if (checkRecipeOutputs(recipe, target)) {
                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    private void generateOutputIndex() {
        if (outputIndexMap == null) {
            outputIndexMap = new IdentityHashMap<>();
            for (net.minecraft.recipe.CraftingRecipe recipe : ForgeRegistries.RECIPES) {
                generateOutputIndex0(recipe);
            }
        }
    }

    private void generateOutputIndex0(net.minecraft.recipe.CraftingRecipe recipe) {
        if (recipe instanceof IRecipeViewable) {
            ChangingItemStack changing = ((IRecipeViewable) recipe).getRecipeOutputs();
            for (ItemStackKey stack : changing.getOptions()) {
                appendIndex(stack.baseStack, recipe, outputIndexMap);
            }
        } else {
            ItemStack output = recipe.getRecipeOutput();
            if (!output.isEmpty()) {
                appendIndex(output, recipe, outputIndexMap);
            }
        }
        for (Ingredient ing : recipe.getIngredients()) {
            generateIngredientIndex(recipe, ing, outputIndexMap);
        }
    }

    private static boolean checkRecipeOutputs(net.minecraft.recipe.CraftingRecipe recipe, ItemStack target) {
        if (recipe instanceof IRecipeViewable) {
            ChangingItemStack changing = ((IRecipeViewable) recipe).getRecipeOutputs();
            if (changing.matches(target)) {
                return true;
            }
        } else {
            ItemStack out = StackUtil.asNonNull(recipe.getRecipeOutput());
            if (OreDictionaryStub.itemMatches(target, out, false) || OreDictionaryStub.itemMatches(out, target, false)) {
                return true;
            }
        }
        return false;
    }
}
