package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

public class RecipeLookupHelper {
    public static final List<IStackRecipes> allHandlers = new ArrayList<>();

    static {
        allHandlers.add(GuideSmeltingRecipes.INSTANCE);
        allHandlers.add(GuideCraftingRecipes.INSTANCE);
    }

    public static List<GuidePartFactory> getAllUsages(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : allHandlers) {
            List<GuidePartFactory> recipes = handler.getUsages(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
        }
        return list;
    }

    public static List<GuidePartFactory> getAllRecipes(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (IStackRecipes handler : allHandlers) {
            List<GuidePartFactory> recipes = handler.getRecipes(stack);
            if (recipes != null) {
                list.addAll(recipes);
            }
        }
        return list;
    }
}
