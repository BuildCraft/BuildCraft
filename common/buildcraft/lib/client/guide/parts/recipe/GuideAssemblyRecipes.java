package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.api.BCItems;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IAssemblyRecipeProvider;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ArrayUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;

public enum GuideAssemblyRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(ItemStack stack) {
        List<GuidePartFactory> usages = new ArrayList<>();
        for (AssemblyRecipe recipe : AssemblyRecipeRegistry.INSTANCE.getAllRecipes()) {
            for (ItemStack req : recipe.requiredStacks) {
                if (StackUtil.isCraftingEquivalent(req, stack, false)) {
                    usages.add(new GuideAssemblyFactory(ArrayUtil.toArray(recipe.requiredStacks), recipe.output, recipe.requiredMicroJoules));
                    break;
                }
            }
        }
        for (IAssemblyRecipeProvider adv : AssemblyRecipeRegistry.INSTANCE.getAllRecipeProviders()) {
            if (adv instanceof IRecipeViewable) {
                IRecipeViewable view = (IRecipeViewable) adv;
                ChangingItemStack[] in = view.getRecipeInputs();
                if (ArrayUtil.testForAny(in, c -> c.matches(stack))) {
                    ChangingItemStack out = view.getRecipeOutputs();
                    usages.add(new GuideAssemblyFactory(in, out, null));
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
                recipes.add(new GuideAssemblyFactory(ArrayUtil.toArray(recipe.requiredStacks), recipe.output, recipe.requiredMicroJoules));
            }
        }
        for (IAssemblyRecipeProvider adv : AssemblyRecipeRegistry.INSTANCE.getAllRecipeProviders()) {
            if (adv instanceof IRecipeViewable) {
                IRecipeViewable view = (IRecipeViewable) adv;
                ChangingItemStack out = view.getRecipeOutputs();
                if (out.matches(stack)) {
                    ChangingItemStack[] in = view.getRecipeInputs();
                    recipes.add(new GuideAssemblyFactory(in, out, null));
                }
            }
        }
        return recipes;
    }
}
