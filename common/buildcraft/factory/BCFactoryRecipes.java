/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.recipe.RecipeBuilderShaped;

public class BCFactoryRecipes {
    public static void init() {
        if (Utils.isRegistered(BCFactoryBlocks.autoWorkbenchItems)) {
            ItemStack out = new ItemStack(BCFactoryBlocks.autoWorkbenchItems);
            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
            builder.add("gwg");
            builder.map('w', "craftingTableWood");
            builder.map('g', "gearStone");
            GameRegistry.addRecipe(builder.build());
            GameRegistry.addRecipe(builder.buildRotated());
        }

        if (Utils.isRegistered(BCFactoryBlocks.miningWell)) {
            ItemStack out = new ItemStack(BCFactoryBlocks.miningWell);
            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
            builder.add("iri");
            builder.add("igi");
            builder.add("ipi");
            builder.map('i', "ingotIron");
            builder.map('r', "dustRedstone");
            builder.map('g', "gearIron");
            builder.map('p', Items.IRON_PICKAXE);
            GameRegistry.addRecipe(builder.build());
        }
    }
}
