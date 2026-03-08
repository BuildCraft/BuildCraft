/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public enum GuideCraftingRecipes implements IStackRecipes {
    INSTANCE;

    private static final boolean USE_INDEX = true;

    // private Map<Item, Set<Recipe>> inputIndexMap, outputIndexMap;
    private Map<Item, Set<Recipe<?>>> inputIndexMap, outputIndexMap;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack target) {
//        final Iterable<Recipe> recipes;
        final Iterable<? extends Recipe<?>> recipes;
        if (USE_INDEX) {
            generateInputIndex();
            recipes = inputIndexMap.get(target.getItem());
            if (recipes == null) {
                return ImmutableList.of();
            }
        } else {
            if (Minecraft.getInstance().level == null) {
                return Lists.newArrayList();
            }
//            recipes = ForgeRegistries.RECIPES;
            recipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
        }

        List<GuidePartFactory> list = new ArrayList<>();
//        for (Recipe recipe : recipes)
        for (Recipe<?> recipe : recipes) {
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
//            for (Recipe recipe : ForgeRegistries.RECIPES)
            if (Minecraft.getInstance().level == null) {
                return;
            }
            for (CraftingRecipe recipe : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
                generateInputIndex0(recipe);
            }
        }
    }

    // private void generateInputIndex0(Recipe recipe)
    private void generateInputIndex0(Recipe<?> recipe) {
        for (Ingredient ing : recipe.getIngredients()) {
            generateIngredientIndex(recipe, ing, inputIndexMap);
        }
    }

    private static void generateIngredientIndex(Recipe<?> recipe, Ingredient ing, Map<Item, Set<Recipe<?>>> indexMap) {
        for (ItemStack stack : ing.getItems()) {
            appendIndex(stack, recipe, indexMap);
        }
    }

    private static void appendIndex(ItemStack stack, Recipe<?> recipe, Map<Item, Set<Recipe<?>>> indexMap) {
//        Set<Recipe> list = indexMap.get(stack.getItem());
        Set<Recipe<?>> list = indexMap.get(stack.getItem());
        if (list == null) {
            list = new LinkedHashSet<>();
            indexMap.put(stack.getItem(), list);
        }
        list.add(recipe);
    }

    private static boolean checkRecipeUses(Recipe<?> recipe, @Nonnull ItemStack target) {
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
//        final Iterable<Recipe> recipes;
        final Iterable<? extends Recipe<?>> recipes;
        if (USE_INDEX) {
            generateOutputIndex();
            recipes = outputIndexMap.get(target.getItem());
            if (recipes == null) {
                return ImmutableList.of();
            }

        } else {
            if (Minecraft.getInstance().level == null) {
                return Lists.newArrayList();
            }
//            recipes = ForgeRegistries.RECIPES;
            recipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
        }

        List<GuidePartFactory> list = new ArrayList<>();
//        for (Recipe recipe : recipes)
        for (Recipe<?> recipe : recipes) {
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
            if (Minecraft.getInstance().level == null) {
                return;
            }
//            for (Recipe recipe : ForgeRegistries.RECIPES)
            for (Recipe<?> recipe : Minecraft.getInstance().level.getRecipeManager().getRecipes()) {
                generateOutputIndex0(recipe);
            }
        }
    }

    // private void generateOutputIndex0(IRecipe recipe)
    private void generateOutputIndex0(Recipe<?> recipe) {
        if (recipe instanceof IRecipeViewable) {
            ChangingItemStack changing = ((IRecipeViewable) recipe).getRecipeOutputs();
            for (ItemStackKey stack : changing.getOptions()) {
                appendIndex(stack.baseStack, recipe, outputIndexMap);
            }
        } else {
            ItemStack output = recipe.getResultItem();
            if (!output.isEmpty()) {
                appendIndex(output, recipe, outputIndexMap);
            }
        }
        for (Ingredient ing : recipe.getIngredients()) {
            generateIngredientIndex(recipe, ing, outputIndexMap);
        }
    }

    // private static boolean checkRecipeOutputs(IRecipe recipe, ItemStack target)
    private static boolean checkRecipeOutputs(Recipe<?> recipe, ItemStack target) {
        if (recipe instanceof IRecipeViewable) {
            ChangingItemStack changing = ((IRecipeViewable) recipe).getRecipeOutputs();
            if (changing.matches(target)) {
                return true;
            }
        } else {
            ItemStack out = StackUtil.asNonNull(recipe.getResultItem());
//            if (OreDictionary.itemMatches(target, out, false) || OreDictionary.itemMatches(out, target, false))
            if (Ingredient.of(target).test(out) || Ingredient.of(out).test(target)) {
                return true;
            }
        }
        return false;
    }
}
