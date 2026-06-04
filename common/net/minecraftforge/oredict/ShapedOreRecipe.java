// STUB(R.Chen): Forge ShapedOreRecipe — compile shim. TODO: replace with vanilla shaped recipe + tag ingredients.
package net.minecraftforge.oredict;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import net.minecraftforge.common.crafting.CraftingHelper;

public class ShapedOreRecipe {
    private final ItemStack output;

    public ShapedOreRecipe(ItemStack output, Object... inputs) {
        this.output = output;
    }

    public ShapedOreRecipe(Identifier group, ItemStack output, CraftingHelper.ShapedPrimer primer) {
        this.output = output;
    }

    public ItemStack getRecipeOutput() { return output; }
    public int getRecipeWidth() { return 3; }
    public int getRecipeHeight() { return 3; }

    public boolean matches(InventoryCrafting inventory, World world) {
        // STUB(R.Chen): shaped ore-dict matching deferred — Phase 10
        return false;
    }

    public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.of();
    }
}
