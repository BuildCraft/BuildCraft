/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import com.google.common.collect.ImmutableSet;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import buildcraft.api.BCBlocks;
import buildcraft.api.BCItems;
import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.StackDefinition;

import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.inventory.filter.OreStackFilter;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.RecipeBuilderShaped;

import buildcraft.core.item.ItemPaintbrush_BC8;

public class BCCoreRecipes {
    public static void init() {
        //TODO: define these in json
        if (BCItems.CORE_PAINTBRUSH != null) {
            ItemStack cleanPaintbrush = new ItemStack(BCItems.CORE_PAINTBRUSH);
            Object[] input = { " iw", " gi", "s  ", 's', "stickWood", 'g', "gearWood", 'w', new ItemStack(Blocks.WOOL,
                1, 0), 'i', Items.STRING };
            Converter.addShapedRecipe(cleanPaintbrush, input);

            for (EnumDyeColor colour : EnumDyeColor.values()) {
                ItemPaintbrush_BC8.Brush brush = BCCoreItems.PAINTBRUSH.new Brush(colour);
                ItemStack out = brush.save();
                //GameRegistry.addRecipe(new ShapelessOreRecipe(out, cleanPaintbrush, ColourUtil.getDyeName(colour)));
            }
        }

        if (BCItems.CORE_LIST != null) {
            if (BCBlocks.SILICON_TABLE_ASSEMBLY != null) {
                long mjCost = 2_000 * MjAPI.MJ;
                ImmutableSet<StackDefinition> required = ImmutableSet.of(//
                    ArrayStackFilter.definition(8, Items.PAPER),//
                    OreStackFilter.definition(ColourUtil.getDyeName(EnumDyeColor.GREEN)),//
                    OreStackFilter.definition("dustRedstone")//
                );
                BuildcraftRecipeRegistry.assemblyRecipes.addRecipe(new AssemblyRecipe("list", mjCost, required,
                    new ItemStack(BCItems.CORE_LIST)));
            } else {
                //handled in JSON
            }
        }

        if (BCBlocks.CORE_DECORATED != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("sss");
            builder.add("scs");
            builder.add("sss");

            if (BCItems.BUILDERS_SNAPSHOT != null) {
                builder.map('s', "stone");
                builder.map('c', new ItemStack(BCItems.BUILDERS_SNAPSHOT, 1, 2));
                builder.setResult(new ItemStack(BCBlocks.CORE_DECORATED, 16, EnumDecoratedBlock.BLUEPRINT.ordinal()));
                builder.register();

                builder.map('c', new ItemStack(BCItems.BUILDERS_SNAPSHOT, 1, 0));
                builder.setResult(new ItemStack(BCBlocks.CORE_DECORATED, 16, EnumDecoratedBlock.TEMPLATE.ordinal()));
                builder.register();
            }

            builder.map('s', Blocks.OBSIDIAN);
            builder.map('c', Blocks.REDSTONE_BLOCK);
            builder.setResult(new ItemStack(BCBlocks.CORE_DECORATED, 32, EnumDecoratedBlock.LASER_BACK.ordinal()));
            builder.register();
        }
    }
}
