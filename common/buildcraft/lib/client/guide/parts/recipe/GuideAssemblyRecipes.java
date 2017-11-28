/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.BCBlocks;
import buildcraft.api.recipes.AssemblyRecipe;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;

public enum GuideAssemblyRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack stack) {
        List<GuidePartFactory> usages = new ArrayList<>();
        boolean all = stack.getItem() == Item.getItemFromBlock(BCBlocks.Silicon.ASSEMBLY_TABLE);
        for (AssemblyRecipe recipe : AssemblyRecipeRegistry.REGISTRY.values()) {
            for (ItemStack output: recipe.getOutputPreviews()) {
                if (all || recipe.getInputsFor(output).stream().anyMatch((definition) -> definition.ingredient.apply(stack))) {
                    usages.add(getFactory(recipe, output));
                }
            }

        }
        return usages;
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack) {
        List<GuidePartFactory> recipes = new ArrayList<>();
        for (AssemblyRecipe recipe : AssemblyRecipeRegistry.REGISTRY.values()) {
            for (ItemStack output: recipe.getOutputPreviews()) {
                if (StackUtil.isCraftingEquivalent(output, stack, false)) {
                    recipes.add(getFactory(recipe, output));
                }
            }
        }
        return recipes;
    }

    private static GuideAssemblyFactory getFactory(AssemblyRecipe recipe, ItemStack output) {
        ChangingItemStack[] stacks = recipe.getInputsFor(output).stream().map(definition -> {
                NonNullList<ItemStack> items = Arrays.stream(definition.ingredient.getMatchingStacks()).map(ItemStack::copy).collect(StackUtil.nonNullListCollector());
                items.forEach(stack -> stack.setCount(definition.count));
                return items;
        }).map(ChangingItemStack::new).toArray(ChangingItemStack[]::new);
        return new GuideAssemblyFactory(stacks, new ChangingItemStack(output), new ChangingObject<>(new Long[] { recipe.getRequiredMicroJoulesFor(output) }));
    }
}
