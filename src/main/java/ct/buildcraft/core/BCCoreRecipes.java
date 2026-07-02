/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ct.buildcraft.core;

import java.util.function.Consumer;

import ct.buildcraft.api.enums.EnumEngineType;
import ct.buildcraft.core.item.ItemPaintbrush_BC8;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class BCCoreRecipes extends RecipeProvider{

	public static void init() {}
    public BCCoreRecipes(DataGenerator p_125973_) {
		super(p_125973_);
	}

		@Override
		protected void buildCraftingRecipes(Consumer<FinishedRecipe> writer) {
	    	if(1!=0) {
	        	ShapedRecipeBuilder builder0 = ShapedRecipeBuilder.shaped(BCCoreItems.PAINT_BRUSH.get(), 1);
	            builder0.pattern(" iw");
	            builder0.pattern(" gi");
	            builder0.pattern("s  ");
	            builder0.define('i', Items.STRING);
	            builder0.define('s', Items.STICK);
	            builder0.define('g', BCCoreItems.GEAR_WOOD.get());
	            builder0.define('w', Blocks.WHITE_WOOL);
	            builder0.unlockedBy("has_"+BCCoreItems.GEAR_WOOD.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_WOOD.get()));
	            builder0.save(writer);

	            for (DyeColor colour : DyeColor.values()) {
	                ItemPaintbrush_BC8 out = BCCoreItems.PAINT_BRUSHS.get(colour);
	                ShapelessRecipeBuilder recipe = new ShapelessRecipeBuilder(out, 1);
	                recipe.requires(BCCoreItems.PAINT_BRUSH.get());
	                recipe.requires(colour.getTag());
	                recipe.unlockedBy("has_"+BCCoreItems.PAINT_BRUSH.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.PAINT_BRUSH.get()));
	                recipe.group("paintbrush_colouring");
	                recipe.save(writer);
	            }
	        }
        	ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(BCCoreItems.GEAR_WOOD.get(), 1);
            builder.pattern(" s ");
            builder.pattern("s s");
            builder.pattern(" s ");
            builder.define('s', Items.STICK);
            builder.unlockedBy("has_"+Items.STICK.getDescriptionId(), TriggerInstance.hasItems(Items.STICK));
            builder.save(writer);
           
        	ShapedRecipeBuilder builder2 = ShapedRecipeBuilder.shaped(BCCoreItems.GEAR_STONE.get(), 1);
            builder2.pattern(" o ");
            builder2.pattern("oio");
            builder2.pattern(" o ");
            builder2.define('o', ItemTags.STONE_TOOL_MATERIALS);
            builder2.define('i', BCCoreItems.GEAR_WOOD.get());
            builder2.unlockedBy("has_"+BCCoreItems.GEAR_WOOD.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_WOOD.get()));
            builder2.save(writer);
            
        	ShapedRecipeBuilder builder3 = ShapedRecipeBuilder.shaped(BCCoreItems.GEAR_GOLD.get(), 1);
            builder3.pattern(" o ");
            builder3.pattern("oio");
            builder3.pattern(" o ");
            builder3.define('o', Items.GOLD_INGOT);
            builder3.define('i', BCCoreItems.GEAR_STONE.get());
            builder3.unlockedBy("has_"+BCCoreItems.GEAR_WOOD.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_WOOD.get()));
            builder3.save(writer);
            
        	ShapedRecipeBuilder builder4 = ShapedRecipeBuilder.shaped(BCCoreItems.GEAR_DIAMOND.get(), 1);
            builder4.pattern(" o ");
            builder4.pattern("oio");
            builder4.pattern(" o ");
            builder4.define('o', Items.DIAMOND);
            builder4.define('i', BCCoreItems.GEAR_GOLD.get());
            builder4.unlockedBy("has_"+BCCoreItems.GEAR_WOOD.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_WOOD.get()));
            builder4.save(writer);
            
        	ShapedRecipeBuilder builder5 = ShapedRecipeBuilder.shaped(BCCoreItems.WRENCH.get(), 1);
            builder5.pattern("I I");
            builder5.pattern(" G ");
            builder5.pattern(" I ");
            builder5.define('I', Items.IRON_INGOT);
            builder5.define('G', BCCoreItems.GEAR_STONE.get());
            builder5.unlockedBy("has_"+BCCoreItems.GEAR_WOOD.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_WOOD.get()));
            builder5.save(writer);
            
        	ShapedRecipeBuilder builder6 = ShapedRecipeBuilder.shaped(BCCoreItems.ENGINE_ITEM_MAP.get(EnumEngineType.WOOD), 1);
            builder6.pattern("www");
            builder6.pattern(" g ");
            builder6.pattern("GpG");
            builder6.define('w', ItemTags.PLANKS);
            builder6.define('g', Items.GLASS);
            builder6.define('G', BCCoreItems.GEAR_WOOD.get());
            builder6.define('p', Blocks.PISTON);
            builder6.unlockedBy("has_"+BCCoreItems.GEAR_WOOD.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_WOOD.get()));
            builder6.save(writer);
            
        	ShapedRecipeBuilder stone_engine = ShapedRecipeBuilder.shaped(BCCoreItems.ENGINE_ITEM_MAP.get(EnumEngineType.STONE), 1);
            stone_engine.pattern("www");
            stone_engine.pattern(" g ");
            stone_engine.pattern("GpG");
            stone_engine.define('w', ItemTags.STONE_CRAFTING_MATERIALS);
            stone_engine.define('g', Items.GLASS);
            stone_engine.define('G', BCCoreItems.GEAR_STONE.get());
            stone_engine.define('p', Blocks.PISTON);
            stone_engine.unlockedBy("has_"+BCCoreItems.GEAR_STONE.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_STONE.get()));
            stone_engine.save(writer);
            
        	ShapedRecipeBuilder iron_engine = ShapedRecipeBuilder.shaped(BCCoreItems.ENGINE_ITEM_MAP.get(EnumEngineType.IRON), 1);
            iron_engine.pattern("www");
            iron_engine.pattern(" g ");
            iron_engine.pattern("GpG");
            iron_engine.define('w', Items.IRON_INGOT);
            iron_engine.define('g', Items.GLASS);
            iron_engine.define('G', BCCoreItems.GEAR_IRON.get());
            iron_engine.define('p', Blocks.PISTON);
            iron_engine.unlockedBy("has_"+BCCoreItems.GEAR_IRON.getId().getPath(), TriggerInstance.hasItems(BCCoreItems.GEAR_IRON.get()));
            iron_engine.save(writer);
            
	    	

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

	        if (false) {
/*	            RecipeBuilderShaped builder = new RecipeBuilderShaped();
	            builder.add("sss");
	            builder.add("scs");
	            builder.add("sss");

	            // if (BCItems.Builders!= null) {
	            // builder.map('s', "stone");
	            // builder.map('c', new ItemStack(BCItems.Builders.SNAPSHOT, 1, 2));
	            // builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16, EnumDecoratedBlock.BLUEPRINT.ordinal()));
	            // builder.register();
	            //
	            // builder.map('c', new ItemStack(BCItems.Builders.SNAPSHOT, 1, 0));
	            // builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16, EnumDecoratedBlock.TEMPLATE.ordinal()));
	            // builder.register();
	            // }

	            builder.map('s', Blocks.OBSIDIAN);
	            builder.map('c', Blocks.REDSTONE_BLOCK);
//	            builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16, EnumDecoratedBlock.LASER_BACK.ordinal()));
	            builder.register();*/
	        }
			super.buildCraftingRecipes(writer);
		}
    	
}
