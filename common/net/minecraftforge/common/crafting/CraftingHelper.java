// STUB(R.Chen): Forge CraftingHelper — compile shim.
package net.minecraftforge.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

public class CraftingHelper {
    public static Ingredient getIngredient(Object obj) { return Ingredient.EMPTY; }
    public static boolean checkRecipe(Object recipe) { return true; }

    /** Compile shim for ShapedPrimer used by BCRecipeShaped. */
    public static class ShapedPrimer {
        public int width, height;
        public boolean mirrored;
        public net.minecraft.util.collection.DefaultedList<Ingredient> input
            = net.minecraft.util.collection.DefaultedList.of();
    }
}
