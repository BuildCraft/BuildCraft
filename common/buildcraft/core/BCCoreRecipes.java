/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.BCBlocks;
import buildcraft.api.BCItems;
import buildcraft.api.enums.EnumEngineType;

import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.OredictionaryNames;
import buildcraft.lib.recipe.RecipeBuilderShaped;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;

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
            Object[] input = { " iw", " gi", "s  ", 's', "stickWood", 'g', "gearWood", 'w', new ItemStack(Blocks.WOOL, 1, 0), 'i', Items.STRING };
            GameRegistry.addRecipe(new ShapedOreRecipe(cleanPaintbrush, input));

            for (EnumDyeColor colour : EnumDyeColor.values()) {
                ItemPaintbrush_BC8.Brush brush = BCCoreItems.paintbrush.new Brush(colour);
                ItemStack out = brush.save();
                GameRegistry.addRecipe(new ShapelessOreRecipe(out, cleanPaintbrush, ColourUtil.getDyeName(colour)));
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
            Object inner = i == 0 ? null : TagManager.getTag("item.gear." + gears[i - 1], EnumTagType.OREDICT_NAME);
            Object outer = outers[i];
            Object[] arr;
            if (inner == null) {
                arr = new Object[] { " o ", "o o", " o ", 'o', outer };
            } else {
                arr = new Object[] { " o ", "oio", " o ", 'o', outer, 'i', inner };
            }
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(gear), arr));
        }
    }
}
