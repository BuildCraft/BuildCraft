// STUB(R.Chen): FurnaceRecipes shim for 1.12.2 → 1.20.1 migration.
package net.minecraft.item.crafting;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.ItemStack;

/** 1.12.2 FurnaceRecipes — stub that returns empty maps. TODO: migrate to RecipeManager. */
public class FurnaceRecipes {
    private static final FurnaceRecipes INSTANCE = new FurnaceRecipes();

    public static FurnaceRecipes instance() { return INSTANCE; }

    public Map<ItemStack, ItemStack> getSmeltingList() {
        return new HashMap<>();
    }

    public ItemStack getSmeltingResult(ItemStack input) {
        return ItemStack.EMPTY;
    }
}
