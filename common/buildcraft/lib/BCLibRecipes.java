package buildcraft.lib;

import buildcraft.api.BCItems;
import buildcraft.lib.recipe.NBTAwareShapedOreRecipe;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BCLibRecipes {
    public static void fmlInit() {
        RecipeSorter.register("buildcraftlib:nbt_aware_shaped_ore", NBTAwareShapedOreRecipe.class, RecipeSorter.Category.SHAPED, "after:forge:shapedore");

        if (BCItems.Lib.GUIDE != null) {
            List<Object> input = new ArrayList<>(4);
            if (BCItems.Core.GEAR_WOOD != null) {
                input.add(BCItems.Core.GEAR_WOOD);
            } else {
                input.add(Items.STICK);
            }
            Collections.addAll(input, Items.PAPER, Items.PAPER, Items.PAPER);
            GameRegistry.addRecipe(new ShapelessOreRecipe(BCItems.Lib.GUIDE, input.toArray()));
        }
    }
}
