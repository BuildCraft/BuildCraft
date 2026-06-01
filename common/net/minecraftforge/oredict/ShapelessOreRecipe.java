// STUB(R.Chen): Forge ShapelessOreRecipe — compile shim. TODO: replace with vanilla shapeless recipe + tag ingredients.
package net.minecraftforge.oredict;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

public class ShapelessOreRecipe {
    private final ItemStack output;

    public ShapelessOreRecipe(ItemStack output, Object... inputs) {
        this.output = output;
    }

    public ItemStack getRecipeOutput() { return output; }
}
