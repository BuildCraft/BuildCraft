/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import buildcraft.api.BCBlocks;
import buildcraft.lib.recipe.OredictionaryNames;
import buildcraft.lib.recipe.RecipeBuilderShaped;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class BCFactoryRecipes {
    public static void init() {
        if (BCBlocks.Factory.AUTOWORKBENCH_ITEM != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("gwg");
            builder.map('w', "craftingTableWood");
            builder.map('g', "gearStone");
            builder.setResult(new ItemStack(BCBlocks.Factory.AUTOWORKBENCH_ITEM));
            builder.register();
            builder.registerRotated();
        }

        if (BCBlocks.Factory.MINING_WELL != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("iri");
            builder.add("igi");
            builder.add("ipi");
            builder.map('i', "ingotIron");
            builder.map('r', "dustRedstone");
            builder.map('g', "gearIron");
            builder.map('p', Items.IRON_PICKAXE);
            builder.setResult(new ItemStack(BCBlocks.Factory.MINING_WELL));
            builder.register();
        }

        if (BCBlocks.Factory.TANK != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("ggg");
            builder.add("g g");
            builder.add("ggg");
            builder.map('g', OredictionaryNames.GLASS_COLOURLESS);
            builder.setResult(new ItemStack(BCBlocks.Factory.TANK));
            builder.register();
        }

        if (BCBlocks.Factory.PUMP != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("iri");
            builder.add("igi");
            builder.add("tbt");
            builder.map('i', "ingotIron");
            builder.map('r', "dustRedstone");
            builder.map('g', "gearIron");
            builder.map('b', Items.BUCKET);
            builder.map('t', BCBlocks.Factory.TANK, OredictionaryNames.GLASS_COLOURLESS);
            builder.setResult(new ItemStack(BCBlocks.Factory.PUMP));
            builder.register();
        }

        if (BCBlocks.Factory.FLOOD_GATE != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("igi");
            builder.add("btb");
            builder.add("ibi");
            builder.map('i', "ingotIron");
            builder.map('g', OredictionaryNames.GEAR_IRON);
            builder.map('b', Blocks.IRON_BARS);
            builder.map('t', BCBlocks.Factory.TANK, OredictionaryNames.GLASS_COLOURLESS);
            builder.setResult(new ItemStack(BCBlocks.Factory.FLOOD_GATE));
            builder.register();
        }

        if (BCBlocks.Factory.CHUTE != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("ici");
            builder.add("igi");
            builder.add(" i ");
            builder.map('i', "ingotIron");
            builder.map('g', OredictionaryNames.GEAR_STONE);
            builder.map('c', Blocks.CHEST);
            builder.setResult(new ItemStack(BCBlocks.Factory.CHUTE));
            builder.register();
        }

        if (BCBlocks.Factory.DISTILLER != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("rtr");
            builder.add("tgt");
            builder.map('r', Blocks.REDSTONE_TORCH);
            builder.map('g', OredictionaryNames.GEAR_DIAMOND);
            builder.map('t', BCBlocks.Factory.TANK, OredictionaryNames.GLASS_COLOURLESS);
            builder.setResult(new ItemStack(BCBlocks.Factory.DISTILLER));
            builder.register();
        }

        if (BCBlocks.Factory.HEAT_EXCHANGE != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("IGI");
            builder.add("###");
            builder.add("IGI");
            builder.map('I', "ingotIron");
            builder.map('G', OredictionaryNames.GEAR_IRON);
            builder.map('#', "blockGlassColorless");
            builder.setResult(new ItemStack(BCBlocks.Factory.HEAT_EXCHANGE));
            builder.register();
        }
    }
}
