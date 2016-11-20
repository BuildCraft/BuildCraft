/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.lib.recipe.RecipeBuilderShaped;

public class BCBuildersRecipes {
    public static void init() {
        ItemStack paper = new ItemStack(Items.PAPER);

        if (BCBuildersItems.schematicSingle != null) {
            ItemStack out = new ItemStack(BCBuildersItems.schematicSingle);
            ShapelessOreRecipe recipe = new ShapelessOreRecipe(out, "gemLapis", "gemLapis");
            GameRegistry.addRecipe(recipe);
        }

        if (BCBuildersItems.blueprint != null) {
            ItemStack out = new ItemStack(BCBuildersItems.blueprint);
            RecipeBuilderShaped recipe = new RecipeBuilderShaped(out);
            recipe.add("ppp");
            recipe.add("plp");
            recipe.add("ppp");
            recipe.map('l', "gemLapis");
            recipe.map('p', paper);
            GameRegistry.addRecipe(recipe.build());
        }

        // if (BCBuildersItems.template != null) {
        // ItemStack out = new ItemStack(BCBuildersItems.template);
        // RecipeBuilderShaped recipe = new RecipeBuilderShaped(out);
        // recipe.add("ppp");
        // recipe.add("pip");
        // recipe.add("ppp");
        // recipe.map('i', "dyeBlack");
        // recipe.map('p', paper);
        // GameRegistry.addRecipe(recipe.build());
        // }
    }
}
