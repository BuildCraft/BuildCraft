/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib;

import buildcraft.api.BCItems;
import buildcraft.lib.recipe.NBTAwareShapedOreRecipe;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BCLibRecipes {
    public static void fmlInit() {
        RecipeSorter.register("buildcraftlib:nbt_aware_shaped_ore", NBTAwareShapedOreRecipe.class, Category.SHAPED, "after:forge:shapedore");

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
