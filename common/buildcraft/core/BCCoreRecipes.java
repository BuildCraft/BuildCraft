/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import buildcraft.api.BCBlocks;
import buildcraft.api.BCItems;
import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.StackDefinition;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.inventory.filter.OreStackFilter;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.OredictionaryNames;
import buildcraft.lib.recipe.RecipeBuilderShaped;
import buildcraft.lib.registry.TagManager;
import com.google.common.collect.ImmutableSet;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class BCCoreRecipes {
    public static void init() {
        if (BCItems.CORE_WRENCH != null) {
            ItemStack out = new ItemStack(BCItems.CORE_WRENCH);
            Object[] in = { "I I", " G ", " I ", 'I', "ingotIron", 'G', "gearStone" };
            GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
        }

        if (BCItems.CORE_DIAMOND_SHARD != null) {
            ItemStack out = new ItemStack(BCItems.CORE_DIAMOND_SHARD, 4);
            Object[] in = { "D", 'D', Items.DIAMOND };
            GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
        }

        if (BCBlocks.CORE_MARKER_VOLUME != null) {
            ItemStack out = new ItemStack(BCBlocks.CORE_MARKER_VOLUME);
            ItemStack lapisLazuli = new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage());
            Object[] in = { "l", "t", 'l', lapisLazuli, 't', Blocks.REDSTONE_TORCH };
            GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
        }

        if (BCBlocks.CORE_MARKER_PATH != null) {
            ItemStack out = new ItemStack(BCBlocks.CORE_MARKER_PATH);
            ItemStack cactusGreen = new ItemStack(Items.DYE, 1, EnumDyeColor.GREEN.getDyeDamage());
            Object[] in = { "g", "t", 'g', cactusGreen, 't', Blocks.REDSTONE_TORCH };
            GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
        }

        if (BCItems.CORE_MARKER_CONNECTOR != null) {
            ItemStack out = new ItemStack(BCItems.CORE_MARKER_CONNECTOR);
            Item wrench;
            if (BCItems.CORE_WRENCH != null) wrench = BCItems.CORE_WRENCH;
            else wrench = Items.IRON_INGOT;

            Item gear;
            if (BCItems.CORE_GEAR_WOOD != null) gear = BCItems.CORE_GEAR_WOOD;
            else gear = Items.STICK;

            Object[] in = { "r", "g", "w", 'r', Blocks.REDSTONE_TORCH, 'g', gear, 'w', wrench };
            GameRegistry.addRecipe(new ShapedOreRecipe(out, in));
        }

        if (BCItems.CORE_PAINTBRUSH != null) {
            ItemStack cleanPaintbrush = new ItemStack(BCItems.CORE_PAINTBRUSH);
            Object[] input = { " iw", " gi", "s  ", 's', "stickWood", 'g', "gearWood", 'w', new ItemStack(Blocks.WOOL,
                    1, 0), 'i', Items.STRING };
            GameRegistry.addRecipe(new ShapedOreRecipe(cleanPaintbrush, input));

            for (EnumDyeColor colour : EnumDyeColor.values()) {
                ItemPaintbrush_BC8.Brush brush = BCCoreItems.paintbrush.new Brush(colour);
                ItemStack out = brush.save();
                GameRegistry.addRecipe(new ShapelessOreRecipe(out, cleanPaintbrush, ColourUtil.getDyeName(colour)));
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
                RecipeBuilderShaped recipe = new RecipeBuilderShaped();
                recipe.add("pRp");
                recipe.add("pGp");
                recipe.add("ppp");
                recipe.map('p', Items.PAPER);
                recipe.map('G', ColourUtil.getDyeName(EnumDyeColor.GREEN));
                recipe.map('R', "dustRedstone");
                recipe.setResult(new ItemStack(BCItems.CORE_LIST));
                recipe.register();
            }
        }

        if (BCCoreBlocks.engine != null && BCCoreBlocks.engine.isRegistered(EnumEngineType.WOOD)) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("www");
            builder.add(" g ");
            builder.add("GpG");
            builder.map('w', "plankWood");
            builder.map('g', OredictionaryNames.GLASS_COLOURLESS);
            builder.map('G', OredictionaryNames.GEAR_WOOD);
            builder.map('p', Blocks.PISTON);
            builder.setResult(BCCoreBlocks.engine.getStack(EnumEngineType.WOOD));
            builder.register();
        }

        String[] gears = { "wood", "stone", "iron", "gold", "diamond" };
        Object[] outers = { "stickWood", "cobblestone", "ingotIron", "ingotGold", "gemDiamond" };
        for (int i = 0; i < gears.length; i++) {
            String key = gears[i];
            Item gear = TagManager.getItem("item.gear." + key);
            if (gear == null) continue;
            Object inner = i == 0 ? null : TagManager.getTag("item.gear." + gears[i - 1], TagManager.EnumTagType.OREDICT_NAME);
            Object outer = outers[i];
            Object[] arr;
            if (inner == null) {
                arr = new Object[] { " o ", "o o", " o ", 'o', outer };
            } else {
                arr = new Object[] { " o ", "oio", " o ", 'o', outer, 'i', inner };
            }
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(gear), arr));
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
