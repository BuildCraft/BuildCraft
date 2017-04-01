package buildcraft.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Items;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.BCItems;

public class BCLibRecipes {
    public static void fmlInit() {
        if (BCItems.LIB_GUIDE != null) {
            List<Object> input = new ArrayList<>(4);
            if (BCItems.CORE_GEAR_WOOD != null) {
                input.add(BCItems.CORE_GEAR_WOOD);
            } else {
                input.add(Items.STICK);
            }
            Collections.addAll(input, Items.PAPER, Items.PAPER, Items.PAPER);
            GameRegistry.addRecipe(new ShapelessOreRecipe(BCItems.LIB_GUIDE, input.toArray()));
        }
    }
}
