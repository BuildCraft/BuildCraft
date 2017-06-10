/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import buildcraft.api.enums.EnumSnapshotType;

import buildcraft.lib.recipe.OredictionaryNames;
import buildcraft.lib.recipe.RecipeBuilderShaped;

public class BCBuildersRecipes {
    public static void init() {
        ItemStack paper = new ItemStack(Items.PAPER);

//        if (BCBuildersItems.schematicSingle != null) {
//            ItemStack out = new ItemStack(BCBuildersItems.schematicSingle, 4);
//            ShapelessOreRecipe recipe = new ShapelessOreRecipe(out, paper, paper, "gemLapis");
//            GameRegistry.addRecipe(recipe);
//        }

        if (BCBuildersItems.snapshot != null) {
            RecipeBuilderShaped recipe = new RecipeBuilderShaped();
            recipe.add("ppp");
            recipe.add("plp");
            recipe.add("ppp");
            recipe.map('l', "dyeBlack");
            recipe.map('p', paper);
            recipe.setResult(BCBuildersItems.snapshot.getClean(EnumSnapshotType.TEMPLATE));
            recipe.register();
        }

        if (BCBuildersItems.snapshot != null) {
            RecipeBuilderShaped recipe = new RecipeBuilderShaped();
            recipe.add("ppp");
            recipe.add("plp");
            recipe.add("ppp");
            recipe.map('l', "gemLapis");
            recipe.map('p', paper);
            recipe.setResult(BCBuildersItems.snapshot.getClean(EnumSnapshotType.BLUEPRINT));
            recipe.register();
        }

        if (BCBuildersBlocks.quarry != null) {
            RecipeBuilderShaped recipe = new RecipeBuilderShaped();
            recipe.add("iri");
            recipe.add("gig");
            recipe.add("dpd");
            recipe.map('i', OredictionaryNames.GEAR_IRON);
            recipe.map('g', OredictionaryNames.GEAR_GOLD);
            recipe.map('d', OredictionaryNames.GEAR_DIAMOND);
            recipe.map('r', "dustRedstone");
            recipe.map('p', Items.DIAMOND_PICKAXE);
            recipe.setResult(new ItemStack(BCBuildersBlocks.quarry));
            recipe.register();
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
