// STUB(R.Chen): Forge ShapedOreRecipe — compile shim. TODO: replace with vanilla shaped recipe + tag ingredients.
package net.minecraftforge.oredict;

import net.minecraft.item.ItemStack;

public class ShapedOreRecipe {
    private final ItemStack output;

    public ShapedOreRecipe(ItemStack output, Object... inputs) {
        this.output = output;
    }

    public ItemStack getRecipeOutput() { return output; }
    public int getRecipeWidth() { return 3; }
    public int getRecipeHeight() { return 3; }
}
