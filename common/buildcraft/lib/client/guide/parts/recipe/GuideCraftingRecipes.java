package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.StackUtil;

public enum GuideCraftingRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(ItemStack target) {
        List<GuidePartFactory> list = new ArrayList<>();

        for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
            if (checkRecipeUses(recipe, target)) {
                GuideCraftingFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    private static boolean checkRecipeUses(IRecipe recipe, @Nonnull ItemStack target) {
        if (recipe instanceof ShapedRecipes) {
            ShapedRecipes shaped = (ShapedRecipes) recipe;
            for (ItemStack in : shaped.recipeItems) {
                if (StackUtil.doesEitherStackMatch(StackUtil.asNonNull(in), target)) {
                    return true;
                }
            }
        } else if (recipe instanceof ShapelessRecipes) {
            ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
            for (ItemStack in : shapeless.recipeItems) {
                if (StackUtil.doesEitherStackMatch(StackUtil.asNonNull(in), target)) {
                    return true;
                }
            }
        } else if (recipe instanceof ShapedOreRecipe) {
            ShapedOreRecipe ore = (ShapedOreRecipe) recipe;
            for (Object in : ore.getInput()) {
                if (matches(target, in)) {
                    return true;
                }
            }
        } else if (recipe instanceof ShapelessOreRecipe) {
            ShapelessOreRecipe ore = (ShapelessOreRecipe) recipe;
            for (Object in : ore.getInput()) {
                if (matches(target, in)) {
                    return true;
                }
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

        for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
            ItemStack out = StackUtil.asNonNull(recipe.getRecipeOutput());
            if (OreDictionary.itemMatches(target, out, false) || OreDictionary.itemMatches(out, target, false)) {
                GuideCraftingFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }

        return list;
    }
}
