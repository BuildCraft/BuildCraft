package buildcraft.lib;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.BCItems;

public class BCLibRecipes {
    public static void fmlInit() {
        if (BCLibItems.guide != null) {
            Object[] input = { new ItemStack(Items.BOOK), null };
            if (BCItems.CORE_GEAR_WOOD != null) {
                input[1] = BCItems.CORE_GEAR_WOOD;
            } else {
                input[1] = Items.STICK;
            }
            GameRegistry.addRecipe(new ShapelessOreRecipe(BCLibItems.guide, input));
        }
    }
}
