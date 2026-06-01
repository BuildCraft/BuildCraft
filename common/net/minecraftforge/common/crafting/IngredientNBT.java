// STUB(R.Chen): Forge IngredientNBT — compile shim.
package net.minecraftforge.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

public class IngredientNBT extends Ingredient {
    public IngredientNBT(ItemStack stack) { super(java.util.stream.Stream.of(new StackEntry(stack))); }
}
