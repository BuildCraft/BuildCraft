package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.lib.client.guide.parts.GuideCraftingFactory;
import buildcraft.lib.client.guide.parts.GuidePartFactory;

public enum GuideCraftingRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();

        for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
            if (checkRecipeUses(recipe, stack)) {
                GuideCraftingFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }

        return list;
    }

    private static boolean checkRecipeUses(IRecipe recipe, ItemStack stack) {
        if (recipe instanceof ShapedRecipes) {
            ShapedRecipes shaped = (ShapedRecipes) recipe;
            for (ItemStack in : shaped.recipeItems) {
                if (OreDictionary.itemMatches(in, stack, false)) {
                    return true;
                }
            }
        } else if (recipe instanceof ShapelessRecipes) {
            ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
            for (ItemStack in : shapeless.recipeItems) {
                if (OreDictionary.itemMatches(in, stack, false)) {
                    return true;
                }
            }
        } else if (recipe instanceof ShapedOreRecipe) {
            ShapedOreRecipe ore = (ShapedOreRecipe) recipe;
            for (Object in : ore.getInput()) {
                if (matches(in, stack)) {
                    return true;
                }
            }
        } else if (recipe instanceof ShapelessOreRecipe) {
            ShapelessOreRecipe ore = (ShapelessOreRecipe) recipe;
            for (Object in : ore.getInput()) {
                if (matches(in, stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matches(Object in, ItemStack stack) {
        if (in instanceof ItemStack) {
            return OreDictionary.itemMatches((ItemStack) in, stack, false);
        } else if (in instanceof List) {
            for (ItemStack inStack : (List<ItemStack>) in) {
                if (OreDictionary.itemMatches(inStack, stack, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<GuidePartFactory> getRecipes(ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();

        for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
            ItemStack out = recipe.getRecipeOutput();
            if (ItemStack.areItemsEqual(stack, out) && ItemStack.areItemStackTagsEqual(stack, out)) {
                GuideCraftingFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }

        return list;
    }
}
