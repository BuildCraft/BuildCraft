package buildcraft.lib.recipe;

import javax.annotation.Nonnull;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class BCRecipeShaped extends ShapedOreRecipe {
    private final boolean enabled;

    public BCRecipeShaped(Identifier group, @Nonnull ItemStack result, CraftingHelper.ShapedPrimer primer, boolean enabled) {
        super(group, enabled ? result : ItemStack.EMPTY, primer);
        this.enabled = enabled;
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inventory, @Nonnull World world) {
        return enabled && super.matches(inventory, world);
    }

    @Nonnull
    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return enabled ? super.getIngredients() : DefaultedList.create();
    }

    @Override
    public boolean isDynamic() {
        return !enabled;
    }
}
