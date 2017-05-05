package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import buildcraft.api.recipes.StackDefinition;
import buildcraft.lib.recipe.ChangingObject;
import net.minecraft.item.ItemStack;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IAssemblyRecipeProvider;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ArrayUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable.IRecipePowered;
import net.minecraft.util.NonNullList;

public enum GuideAssemblyRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(ItemStack stack) {
        List<GuidePartFactory> usages = new ArrayList<>();
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
        return usages;
    }

    @Override
    public List<GuidePartFactory> getRecipes(ItemStack stack) {
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
        ChangingItemStack[] stacks = recipe.requiredStacks.stream().map(definition -> {
                NonNullList<ItemStack> items = definition.filter.getExamples().stream().map(ItemStack::copy).collect(StackUtil.nonNullListCollector());
                items.forEach(stack -> stack.setCount(definition.count));
                return items;
        }).map(ChangingItemStack::new).toArray(ChangingItemStack[]::new);
        return new GuideAssemblyFactory(stacks, ChangingItemStack.create(recipe.output), new ChangingObject<>(new Long[] { recipe.requiredMicroJoules }));
    }
}
