/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport;

import java.util.function.Consumer;

import ct.buildcraft.core.BCCoreItems;
import ct.buildcraft.energy.BCEnergyFluids;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class BCTransportRecipesProvider extends RecipeProvider{

	private final Item waterProof ;
	
	public BCTransportRecipesProvider(DataGenerator p_125973_) {
		super(p_125973_);
		waterProof = BCTransportItems.WATER_PROOF.get();
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> writer) {                
		creatPipeRecipes(writer, ItemTags.PLANKS, BCTransportItems.PIPE_ITEM_WOOD.get(), BCTransportItems.PIPE_FLUID_WOOD.get(), BCTransportItems.PIPE_POWER_WOOD.get());
		creatPipeRecipes(writer, ItemTags.STONE_TOOL_MATERIALS, BCTransportItems.PIPE_ITEM_COBBLE.get(), BCTransportItems.PIPE_FLUID_COBBLE.get(), BCTransportItems.PIPE_POWER_COBBLE.get());
		creatPipeRecipes(writer, Items.STONE, BCTransportItems.PIPE_ITEM_STONE.get(), BCTransportItems.PIPE_FLUID_STONE.get(), BCTransportItems.PIPE_POWER_STONE.get());
        creatPipeRecipes(writer, Items.IRON_INGOT, BCTransportItems.PIPE_ITEM_IRON.get(), BCTransportItems.PIPE_FLUID_IRON.get(), null);
        creatPipeRecipes(writer, Items.GOLD_INGOT, BCTransportItems.PIPE_ITEM_GOLD.get(), BCTransportItems.PIPE_FLUID_GOLD.get(), BCTransportItems.PIPE_POWER_GOLD.get());
        creatPipeRecipes(writer, Items.DIAMOND, BCTransportItems.PIPE_ITEM_DIAMOND.get(), BCTransportItems.PIPE_FLUID_DIAMOND.get(), null);
        creatPipeRecipes(writer, Items.SANDSTONE, BCTransportItems.PIPE_ITEM_SAND_STONE.get(), BCTransportItems.PIPE_FLUID_SAND_STONE.get(), BCTransportItems.PIPE_POWER_SAND_STONE.get());
        creatPipeRecipes(writer, Items.QUARTZ_BLOCK, BCTransportItems.PIPE_ITEM_QUARTZ.get(), BCTransportItems.PIPE_FLUID_QUARTZ.get(), BCTransportItems.PIPE_POWER_QUARTZ.get());
        creatPipeRecipes(writer, Items.CLAY, BCTransportItems.PIPE_ITEM_CLAY.get(), BCTransportItems.PIPE_FLUID_CLAY.get(), null);
        creatPipeRecipes(writer, Items.LAPIS_BLOCK, BCTransportItems.PIPE_ITEM_LAPIS.get(), null, null);
        creatPipeRecipes(writer, Items.OBSIDIAN, BCTransportItems.PIPE_ITEM_OBSIDIAN.get(), null, null);
        creatPipeRecipes(writer, Items.EMERALD, BCTransportItems.PIPE_ITEM_DIAWOOD.get(), BCTransportItems.PIPE_FLUID_DIAWOOD.get(), null);
        creatPipeRecipes(writer, BCCoreItems.GEAR_GOLD.get(), BCTransportItems.PIPE_ITEM_STRIPES.get(), null, null);
        creatSpecPipeRecipes(writer, Items.LAPIS_BLOCK, Items.EMERALD, BCTransportItems.PIPE_ITEM_EMZULI.get(), null, null);
        creatSpecPipeRecipes(writer, Items.LAPIS_BLOCK, Items.DIAMOND, BCTransportItems.PIPE_ITEM_DAIZULI.get(), null, null);
        creatSpecPipeRecipes(writer, Items.INK_SAC, Items.REDSTONE, BCTransportItems.PIPE_ITEM_VOID.get(), BCTransportItems.PIPE_FLUID_VOID.get(), null);
        creatStructurePipeRecipes(writer, Items.COBBLESTONE, Items.GRAVEL, BCTransportItems.PIPE_STRUCTURE.get());
        creatPowerAdapterRecipes(writer);
        creatPlugBlockerRecipes(writer);
        creatWaterProofRecipe(writer);
        creatFilterBufferRecipe(writer);
	}
	
	private void creatPipeRecipes(Consumer<FinishedRecipe> writer, ItemLike mat, Item itemOutput, Item fluidOutput, Item powerOutput) {
    	ShapedRecipeBuilder builder6 = ShapedRecipeBuilder.shaped(itemOutput, 8);
        builder6
        .pattern("mgm")
        .define('g', Items.GLASS)
        .define('m', mat)
        .unlockedBy("has_"+Items.GLASS.getDescriptionId(), TriggerInstance.hasItems(Items.GLASS))
        .save(writer);
        if(fluidOutput != null)
        ShapelessRecipeBuilder.shapeless(fluidOutput)
		  .requires(itemOutput) // 将物品加入配方
		  .requires(waterProof)
		  .unlockedBy("has_"+waterProof.getDescriptionId(), TriggerInstance.hasItems(waterProof)) // 该配方如何解锁
		  .save(writer); // 将数据加入生成器
        if(powerOutput != null)
        ShapelessRecipeBuilder.shapeless(powerOutput)
        		  .requires(itemOutput) // 将物品加入配方
        		  .requires(Items.REDSTONE)
        		  .unlockedBy("has_"+Items.REDSTONE, TriggerInstance.hasItems(Items.REDSTONE)) // 该配方如何解锁
        		  .save(writer); // 将数据加入生成器
	}
	
	private void creatPipeRecipes(Consumer<FinishedRecipe> writer, TagKey<Item> mat, Item itemOutput, Item fluidOutput, Item powerOutput) {
    	ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(itemOutput, 8);
        builder
        .pattern("mgm")
        .define('g', Items.GLASS)
        .define('m', mat)
        .unlockedBy("has_"+Items.GLASS.getDescriptionId(), TriggerInstance.hasItems(Items.GLASS))
        .save(writer);
        if(fluidOutput != null)
        ShapelessRecipeBuilder.shapeless(fluidOutput)
        		  .requires(itemOutput) // 将物品加入配方
        		  .requires(waterProof)
        		  .unlockedBy("has_"+waterProof.getDescriptionId(), TriggerInstance.hasItems(waterProof)) // 该配方如何解锁
        		  .save(writer); // 将数据加入生成器
        if(powerOutput != null)
        ShapelessRecipeBuilder.shapeless(powerOutput)
        		  .requires(itemOutput) // 将物品加入配方
        		  .requires(Items.REDSTONE)
        		  .unlockedBy("has_"+Items.REDSTONE, TriggerInstance.hasItems(Items.REDSTONE)) // 该配方如何解锁
        		  .save(writer); // 将数据加入生成器
	}
	
	private void creatSpecPipeRecipes(Consumer<FinishedRecipe> writer, ItemLike left, ItemLike right, Item itemOutput, Item fluidOutput, Item powerOutput) {
    	ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(itemOutput, 8);
        builder
        .pattern("mgn")
        .define('g', Items.GLASS)
        .define('m', left)
        .define('n', right)
        .unlockedBy("has_"+Items.GLASS.getDescriptionId(), TriggerInstance.hasItems(Items.GLASS))
        .save(writer);
        if(fluidOutput != null)
        ShapelessRecipeBuilder.shapeless(fluidOutput)
        		  .requires(itemOutput) // 将物品加入配方
        		  .requires(waterProof)
        		  .unlockedBy("has_"+waterProof.getDescriptionId(), TriggerInstance.hasItems(waterProof)) // 该配方如何解锁
        		  .save(writer); // 将数据加入生成器
        if(powerOutput != null)
        ShapelessRecipeBuilder.shapeless(powerOutput)
        		  .requires(itemOutput) // 将物品加入配方
        		  .requires(Items.REDSTONE)
        		  .unlockedBy("has_"+Items.REDSTONE, TriggerInstance.hasItems(Items.REDSTONE)) // 该配方如何解锁
        		  .save(writer); // 将数据加入生成器
	}
	
	private void creatStructurePipeRecipes(Consumer<FinishedRecipe> writer, ItemLike stone,ItemLike center, Item itemOutput) {
    	ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(itemOutput, 8);
        builder
        .pattern("mgm")
        .define('g', center)
        .define('m', stone)
        .unlockedBy("has_"+Items.GLASS.getDescriptionId(), TriggerInstance.hasItems(Items.GLASS))
        .save(writer);
	}
	
	private void creatPowerAdapterRecipes(Consumer<FinishedRecipe> writer) {
    	ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(BCTransportItems.plugPowerAdaptor.get(), 1);
        builder
        .pattern("sgs")
        .pattern("ses")
        .pattern("srs")
        .define('g', Items.GOLD_INGOT)
        .define('s', BCTransportItems.PIPE_STRUCTURE.get())
        .define('e', BCCoreItems.GEAR_STONE.get())
        .define('r', Items.REDSTONE)
        .unlockedBy("has_"+Items.GLASS.getDescriptionId(), TriggerInstance.hasItems(Items.GLASS))
        .save(writer);
	}
	
	private void creatPlugBlockerRecipes(Consumer<FinishedRecipe> writer) {
        ShapelessRecipeBuilder.shapeless(BCTransportItems.plugBlocker.get(), 4)
		  .requires(BCTransportItems.PIPE_STRUCTURE.get()) // 将物品加入配方
		  .unlockedBy("has_"+BCTransportItems.PIPE_STRUCTURE.get(), TriggerInstance.hasItems(BCTransportItems.PIPE_STRUCTURE.get())) // 该配方如何解锁
		  .save(writer); // 将数据加入生成器
	}
	
	private void creatWaterProofRecipe(Consumer<FinishedRecipe> writer) {
        ShapelessRecipeBuilder.shapeless(BCTransportItems.WATER_PROOF.get(), 1)
		  .requires(Ingredient.of(Items.SLIME_BALL,Items.GREEN_DYE)) // 将物品加入配方
		  .unlockedBy("has_"+BCTransportItems.PIPE_ITEM_WOOD.get(), TriggerInstance.hasItems(BCTransportItems.PIPE_ITEM_WOOD.get())) // 该配方如何解锁
		  .save(writer); // 将数据加入生成器
        ShapelessRecipeBuilder.shapeless(BCTransportItems.WATER_PROOF.get(), 8)
		  .requires(BCEnergyFluids.OIL_BUCKET.get(12).get()) // 将物品加入配方
		  .unlockedBy("has_"+BCTransportItems.PIPE_ITEM_WOOD.get(), TriggerInstance.hasItems(BCTransportItems.PIPE_ITEM_WOOD.get())) // 该配方如何解锁
		  .save(writer, new ResourceLocation("buildcrafttransport","residue_to_waterproof")); // 将数据加入生成器*/
/*    	ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(BCTransportItems.WATER_PROOF.get(), 8);
        builder.pattern("s");
        builder.define('s', BCEnergyFluids.OIL_BUCKET.get(12).get());
        builder.unlockedBy("has_"+BCTransportItems.PIPE_ITEM_WOOD.get(), TriggerInstance.hasItems(BCTransportItems.PIPE_ITEM_WOOD.get()));
        builder.save(writer);//*/
        
	}
	
	private void creatFilterBufferRecipe(Consumer<FinishedRecipe> writer) {
    	ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(BCTransportBlocks.FILTERED_BUFFER_ITEM.get(), 1);
        builder
        .pattern("sgs")
        .pattern("ses")
        .pattern("srs")
        .define('g', BCTransportItems.PIPE_ITEM_DIAMOND.get())
        .define('s', ItemTags.PLANKS)
        .define('e', Items.CHEST)
        .define('r', Items.PISTON)
        .unlockedBy("has_"+BCTransportItems.PIPE_ITEM_DIAMOND.get(), TriggerInstance.hasItems(BCTransportItems.PIPE_ITEM_DIAMOND.get()))
        .save(writer);
	}
}
