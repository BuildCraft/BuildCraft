package buildcraft.transport;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class BCTransportRecipes {
    public static void init() {
        if (BCTransportItems.waterproof != null) {
            GameRegistry.addShapelessRecipe(new ItemStack(BCTransportItems.waterproof), new ItemStack(Items.DYE, 1, 2));
        }
    }
}
