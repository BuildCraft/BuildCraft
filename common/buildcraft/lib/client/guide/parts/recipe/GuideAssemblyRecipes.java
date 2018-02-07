/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import buildcraft.api.core.BCLog;
import buildcraft.api.items.BCStackHelper;
import net.minecraft.item.ItemStack;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IAssemblyRecipeProvider;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ArrayUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;
import buildcraft.lib.recipe.IRecipeViewable.IRecipePowered;

public enum GuideAssemblyRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack stack) {
        List<GuidePartFactory> usages = new ArrayList<>();
        if (!BCStackHelper.isEmpty(stack)) {
            BCLog.logger.info("Getting usages for " + stack.getUnlocalizedName());
            for (AssemblyRecipe recipe : AssemblyRecipeRegistry.INSTANCE.getAllRecipes()) {
                if (recipe.requiredStacks.stream().anyMatch((definition) -> definition.filter.matches(stack))) {
                    usages.add(getFactory(recipe));
                }
            }
            for (IAssemblyRecipeProvider adv : AssemblyRecipeRegistry.INSTANCE.getAllRecipeProviders()) {
                if (adv instanceof IRecipePowered) {
                    IRecipePowered view = (IRecipePowered) adv;
                    ChangingItemStack[] in = view.getRecipeInputs();
                    if (ArrayUtil.testForAny(in, c -> c.matches(stack))) {
                        ChangingItemStack out = view.getRecipeOutputs();
                        usages.add(new GuideAssemblyFactory(in, out, view.getMjCost()));
                    }
                }
            }
        }
        return usages;
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack) {
        List<GuidePartFactory> recipes = new ArrayList<>();
        for (AssemblyRecipe recipe : AssemblyRecipeRegistry.INSTANCE.getAllRecipes()) {
            if (StackUtil.isCraftingEquivalent(recipe.output, stack, false)) {
                recipes.add(getFactory(recipe));
            }
        }
        for (IAssemblyRecipeProvider adv : AssemblyRecipeRegistry.INSTANCE.getAllRecipeProviders()) {
            if (adv instanceof IRecipePowered) {
                IRecipePowered view = (IRecipePowered) adv;
                ChangingItemStack out = view.getRecipeOutputs();
                if (out.matches(stack)) {
                    ChangingItemStack[] in = view.getRecipeInputs();
                    recipes.add(new GuideAssemblyFactory(in, out, view.getMjCost()));
                }
            }
        }
        return recipes;
    }

    private GuideAssemblyFactory getFactory(AssemblyRecipe recipe) {
        BCLog.logger.info("Getting recipe factory for " + recipe.name);
        ChangingItemStack[] stacks = recipe.requiredStacks.stream().map(definition -> {
            List<ItemStack> items = definition.filter.getExamples().stream().filter(Objects::nonNull).map(ItemStack::copy).collect(Collectors.toList());
            items.forEach(stack -> stack.stackSize = definition.count);
            return items;
        }).map(ChangingItemStack::new).toArray(ChangingItemStack[]::new);
        return new GuideAssemblyFactory(stacks, ChangingItemStack.create(recipe.output), new ChangingObject<>(new Long[] { recipe.requiredMicroJoules }));
    }
}
