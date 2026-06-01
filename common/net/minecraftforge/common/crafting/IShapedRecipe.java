// STUB(R.Chen): Forge IShapedRecipe — compile shim.
package net.minecraftforge.common.crafting;

import net.minecraft.recipe.CraftingRecipe;

public interface IShapedRecipe<C extends net.minecraft.inventory.RecipeInputInventory> extends CraftingRecipe {
    int getRecipeWidth();
    int getRecipeHeight();
}
