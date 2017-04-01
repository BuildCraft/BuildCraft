/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.api.BCBlocks;

import buildcraft.lib.recipe.OredictionaryNames;
import buildcraft.lib.recipe.RecipeBuilderShaped;

public class BCFactoryRecipes {
    public static void init() {
        if (BCBlocks.FACTORY_AUTOWORKBENCH_ITEM != null) {
            ItemStack out = new ItemStack(BCBlocks.FACTORY_AUTOWORKBENCH_ITEM);
            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
            builder.add("gwg");
            builder.map('w', "craftingTableWood");
            builder.map('g', "gearStone");
            GameRegistry.addRecipe(builder.build());
            GameRegistry.addRecipe(builder.buildRotated());
        }

        if (BCBlocks.FACTORY_MINING_WELL != null) {
            ItemStack out = new ItemStack(BCBlocks.FACTORY_MINING_WELL);
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

        if (BCBlocks.FACTORY_TANK != null) {
            ItemStack out = new ItemStack(BCBlocks.FACTORY_TANK);
            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
            builder.add("ggg");
            builder.add("g g");
            builder.add("ggg");
            builder.map('g', "blockGlassColorless");
            GameRegistry.addRecipe(builder.build());
        }

        if (BCBlocks.FACTORY_PUMP != null) {
            ItemStack out = new ItemStack(BCBlocks.FACTORY_PUMP);
            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
            builder.add("iri");
            builder.add("igi");
            builder.add("tbt");
            builder.map('i', "ingotIron");
            builder.map('r', "dustRedstone");
            builder.map('g', "gearIron");
            builder.map('b', Items.BUCKET);
            builder.map('t', BCBlocks.FACTORY_TANK, "blockGlassColorless");
            GameRegistry.addRecipe(builder.build());
        }

        if (BCBlocks.FACTORY_FLOOD_GATE != null) {
            ItemStack out = new ItemStack(BCBlocks.FACTORY_FLOOD_GATE);
            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
            builder.add("igi");
            builder.add("btb");
            builder.add("ibi");
            builder.map('i', "ingotIron");
            builder.map('g', OredictionaryNames.GEAR_IRON);
            builder.map('b', Blocks.IRON_BARS);
            builder.map('t', BCBlocks.FACTORY_TANK, OredictionaryNames.GLASS_COLOURLESS);
            GameRegistry.addRecipe(builder.build());
        }

        if (BCBlocks.FACTORY_CHUTE != null) {
            ItemStack out = new ItemStack(BCBlocks.FACTORY_CHUTE);
            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
            builder.add("ici");
            builder.add("igi");
            builder.add(" i ");
            builder.map('i', "ingotIron");
            builder.map('g', OredictionaryNames.GEAR_STONE);
            builder.map('c', Blocks.CHEST);
            GameRegistry.addRecipe(builder.build());
        }

        if (BCBlocks.FACTORY_DISTILLER != null) {
            ItemStack out = new ItemStack(BCBlocks.FACTORY_DISTILLER);
            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
            builder.add("rtr");
            builder.add("tgt");
            builder.map('r', Blocks.REDSTONE_TORCH);
            builder.map('g', OredictionaryNames.GEAR_DIAMOND);
            builder.map('t', BCBlocks.FACTORY_TANK, OredictionaryNames.GLASS_COLOURLESS);
            GameRegistry.addRecipe(builder.build());
        }

        RecipeBuilderShaped heatExchanger = new RecipeBuilderShaped();
        heatExchanger.add("   ");
        heatExchanger.add("pgp");
        heatExchanger.add("   ");
        if (BCBlocks.FACTORY_HEAT_EXCHANGE_START != null) {

        }
    }
}
