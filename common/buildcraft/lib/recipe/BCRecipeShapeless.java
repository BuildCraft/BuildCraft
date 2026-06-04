package buildcraft.lib.recipe;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import net.minecraftforge.oredict.ShapelessOreRecipe;

public class BCRecipeShapeless extends ShapelessOreRecipe {
    private final boolean enabled;

    public BCRecipeShapeless(Identifier group, DefaultedList<Ingredient> input, @Nonnull ItemStack result, boolean enabled) {
        super(group, input, enabled ? result : ItemStack.EMPTY);
        this.enabled = enabled;
}

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean matches(@Nonnull InventoryCrafting inventory, @Nonnull World world) {
        return enabled && super.matches(inventory, world);
    }

    @Nonnull
    // @Override -- removed: method does not exist in Fabric 1.20.1
    public DefaultedList<Ingredient> getIngredients() {
        return enabled ? super.getIngredients() : DefaultedList.of();
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean isDynamic() {
        return !enabled;
    }
}
