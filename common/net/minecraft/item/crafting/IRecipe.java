// STUB(R.Chen): Minecraft 1.12 IRecipe → Recipe in 1.20.1.
package net.minecraft.item.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;

import java.util.List;

public interface IRecipe {
    boolean matches(CraftingInventory inv, World world);
    ItemStack getCraftingResult(CraftingInventory inv);
    ItemStack getRecipeOutput();
    List<Ingredient> getIngredients();
    boolean isDynamic();
    int getRecipeWidth();
    int getRecipeHeight();
}
