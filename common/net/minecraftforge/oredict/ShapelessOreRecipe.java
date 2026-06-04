// STUB(R.Chen): Forge ShapelessOreRecipe — compile shim. TODO: replace with vanilla shapeless recipe + tag ingredients.
package net.minecraftforge.oredict;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ShapelessOreRecipe {
    private final ItemStack output;
    private final DefaultedList<Ingredient> input;

    public ShapelessOreRecipe(ItemStack output, Object... inputs) {
        this.output = output;
        this.input = DefaultedList.of();
    }

    public ShapelessOreRecipe(Identifier group, DefaultedList<Ingredient> input, ItemStack output) {
        this.output = output;
        this.input = input == null ? DefaultedList.of() : input;
    }

    public ItemStack getRecipeOutput() { return output; }

    public boolean matches(InventoryCrafting inventory, World world) {
        // STUB(R.Chen): shapeless ore-dict matching deferred — Phase 10
        return false;
    }

    public DefaultedList<Ingredient> getIngredients() {
        return input;
    }
}
