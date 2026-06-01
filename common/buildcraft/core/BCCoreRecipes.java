/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import java.util.Set;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
// STUB(R.Chen): // @SubscribeEvent — TODO(R.Chen): port to Fabric event removed — port to Fabric events
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.BCBlocks;
import buildcraft.api.BCItems;
import buildcraft.api.BCModules;
import buildcraft.api.enums.EnumDecoratedBlock;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.RecipeBuilderShaped;

import buildcraft.core.item.ItemPaintbrush_BC8;

public class BCCoreRecipes {

    public static void fmlPreInit() {
        // STUB(R.Chen): MinecraftForge.EVENT_BUS.register(BCCoreRecipes.class);
    }

    // @SubscribeEvent — TODO(R.Chen): port to Fabric event
    public static void registerRecipes(RegistryEvent.Register<net.minecraft.recipe.CraftingRecipe> event) {
        // TODO (1.13): define these in json

        if (BCItems.Core.PAINTBRUSH != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add(" iw");
            builder.add(" gi");
            builder.add("s  ");
            builder.map('i', Items.STRING);
            builder.map('s', "stickWood");
            builder.map('g', "gearWood");
            builder.map('w', new ItemStack(Blocks.WOOL));
            ItemStack cleanPaintbrush = new ItemStack(BCItems.Core.PAINTBRUSH);
            builder.setResult(cleanPaintbrush);
            builder.register();

            for (DyeColor colour : DyeColor.values()) {
                ItemPaintbrush_BC8.Brush brush = BCCoreItems.paintbrush.new Brush(colour);
                ItemStack out = brush.save();

                Object[] inputs = { //
                    cleanPaintbrush, //
                    ColourUtil.getDyeName(colour),//
                };
                Identifier group = BCModules.CORE.createLocation("paintbrush_colouring");
                ShapelessOreRecipe recipe = new ShapelessOreRecipe(group, out, inputs);
                recipe.setRegistryName(BCModules.CORE.createLocation("paintbrush_" + colour.getName()));
                event.getRegistry().register(recipe);
            }
        }

        // if (BCItems.CORE_LIST != null) {
        // if (BCBlocks.SILICON_TABLE_ASSEMBLY != null) {
        // long mjCost = 2_000 * MjAPI.MJ;
        // ImmutableSet<StackDefinition> required = ImmutableSet.of(//
        // ArrayStackFilter.definition(8, Items.PAPER), //
        // OreStackFilter.definition(ColourUtil.getDyeName(DyeColor.GREEN)), //
        // OreStackFilter.definition("dustRedstone")//
        // );
        // BuildcraftRecipeRegistry.assemblyRecipes
        // .addRecipe(new AssemblyRecipe("list", mjCost, required, new ItemStack(BCItems.CORE_LIST)));
        // } else {
        // // handled in JSON
        // }
        // }

        if (BCBlocks.Core.DECORATED != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("sss");
            builder.add("scs");
            builder.add("sss");

            // if (BCItems.Builders!= null) {
            // builder.map('s', "stone");
            // builder.map('c', new ItemStack(BCItems.Builders.SNAPSHOT, 1));
            // builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16));
            // builder.register();
            //
            // builder.map('c', new ItemStack(BCItems.Builders.SNAPSHOT, 1));
            // builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16));
            // builder.register();
            // }

            builder.map('s', Blocks.OBSIDIAN);
            builder.map('c', Blocks.REDSTONE_BLOCK);
            builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16));
            builder.register();
        }
    }
}
