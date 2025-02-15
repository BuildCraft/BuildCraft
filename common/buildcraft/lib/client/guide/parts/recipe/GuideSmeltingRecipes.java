/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.StackUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public enum GuideSmeltingRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack stack) {
        if (Minecraft.getInstance().level == null) {
            return Lists.newArrayList();
        }
//        Map<ItemStack, ItemStack> recipes;
        List<SmeltingRecipe> recipes;
//        Map<ItemStack, ItemStack> old = FurnaceRecipes.instance().getSmeltingList();
        List<SmeltingRecipe> old = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING);
//        recipes = new TreeMap<>(Comparator.comparing(ItemStack::getDisplayName));
//        recipes = new ArrayList<>();
//        recipes.putAll(old);
        recipes = old;
        // Calen: no meta in 1.18.2
//        if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
//            List<GuidePartFactory> list = new ArrayList<>();
//            for (Entry<ItemStack, ItemStack> recipe : recipes.entrySet()) {
//                if (StackUtil.doesEitherStackMatch(stack, StackUtil.asNonNull(recipe.getValue()))//
//                        || StackUtil.doesEitherStackMatch(stack, StackUtil.asNonNull(recipe.getKey()))) {
//                    list.add(new GuideSmeltingFactory(recipe.getKey(), recipe.getValue()));
//                }
//            }
//            return list;
//        }

//        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
        SmeltingRecipe recipe = recipes.stream().filter((r) -> {
            return r.getIngredients().stream().anyMatch(i -> i.test(stack));
        }).findFirst().orElse(null);

//        if (!result.isEmpty())
        if (recipe != null) {
//            return ImmutableList.of(new GuideSmeltingFactory(stack, result));
            return ImmutableList.of(new GuideSmeltingFactory(recipe.getIngredients(), recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
        }

        if (stack.getItem() == Items.FURNACE) {
            List<GuidePartFactory> list = new ArrayList<>();
//            for (Entry<ItemStack, ItemStack> recipe : recipes.entrySet())
            for (SmeltingRecipe recipe_i : recipes) {
//                list.add(new GuideSmeltingFactory(recipe.getKey(), recipe.getValue()));
                list.add(new GuideSmeltingFactory(recipe_i.getIngredients(), recipe_i.getResultItem(Minecraft.getInstance().level.registryAccess())));
            }
            return list;
        }

        return null;
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();

        if (Minecraft.getInstance().level == null) {
            return list;
        }
//        for (Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet())
        for (SmeltingRecipe recipe : Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
//            ItemStack input = StackUtil.asNonNull(entry.getKey());
            NonNullList<Ingredient> input = recipe.getIngredients();
//            ItemStack output = StackUtil.asNonNull(entry.getValue());
            ItemStack output = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
            if (StackUtil.doesEitherStackMatch(stack, output)) {
                list.add(new GuideSmeltingFactory(input, output));
            }
        }

        return list;
    }
}
