/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import ct.buildcraft.api.enums.EnumEngineType;
import ct.buildcraft.api.enums.EnumRedstoneChipset;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.recipes.IngredientStack;
import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.core.BCCoreItems;
import ct.buildcraft.lib.misc.ColourUtil;
import ct.buildcraft.lib.recipe.AssemblyRecipeBuilder;
import ct.buildcraft.lib.recipe.NbtShapedRecipeBuilder;
import ct.buildcraft.silicon.gate.EnumGateLogic;
import ct.buildcraft.silicon.gate.EnumGateMaterial;
import ct.buildcraft.silicon.gate.EnumGateModifier;
import ct.buildcraft.silicon.gate.GateVariant;
import ct.buildcraft.transport.BCTransportItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;

public class BCSiliconRecipesProvider extends RecipeProvider{

	public BCSiliconRecipesProvider(DataGenerator p_125973_) {
		super(p_125973_);
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> writer) { 
		if (BCSiliconItems.PLUG_GATE_ITEM.isPresent()) {
			// You can craft some of the basic gate types in a normal crafting table
			

			// Base craftable types
			
			makeGateRecipe(writer, Ingredient.of(Tags.Items.INGOTS_BRICK), EnumGateMaterial.CLAY_BRICK, EnumGateModifier.NO_MODIFIER);
			
            makeGateRecipe(writer, Ingredient.of(Tags.Items.INGOTS_IRON), EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER);

            makeGateRecipe(writer, Ingredient.of(Tags.Items.INGOTS_NETHER_BRICK), EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER);
            // Iron modifier addition
            
            makeGateRecipe0(writer, Ingredient.of(Tags.Items.DYES_BLUE), EnumGateMaterial.IRON, EnumGateModifier.LAPIS);

            makeGateRecipe0(writer, Ingredient.of(Tags.Items.GEMS_QUARTZ), EnumGateMaterial.IRON, EnumGateModifier.QUARTZ);
            
            // And Gate <-> Or Gate (shapeless)
            // @see{GateLogicChangeRecipe}
            SpecialRecipeBuilder.special(BCSiliconRecipes.GATE_CHANGE_SERIALIZER.get()).save(writer, "buildcraftsilicon:special/gate_logic_change");
            if (BCSiliconItems.PLUG_PULSAR_ITEM.get() != null) {
                ItemStack output = new ItemStack(BCSiliconItems.PLUG_PULSAR_ITEM.get());

                ItemStack redstoneEngine;
                if (BCCoreBlocks.ENGINE_BC8.get() != null) {
                    redstoneEngine = new ItemStack(BCCoreItems.ENGINE_ITEM_MAP.get(EnumEngineType.WOOD));
                } else {
                    redstoneEngine = new ItemStack(Blocks.REDSTONE_BLOCK);
                }

                ImmutableSet.Builder<IngredientStack> input = ImmutableSet.builder();
                input.add(new IngredientStack(Ingredient.of(redstoneEngine)));
                input.add(new IngredientStack(Ingredient.of(Tags.Items.INGOTS_IRON), 2));
                AssemblyRecipeBuilder assemblyBuilder = new AssemblyRecipeBuilder(1000 * MjAPI.MJ, input.build(), output);
                assemblyBuilder.unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
                .save(writer, new ResourceLocation("buildcraftsilicon", "assembly/plug_pulsar"));
                //AssemblyRecipeRegistry.register(recipe);
            }
            if (BCSiliconItems.PLUG_GATE_ITEM.isPresent()) {
                IngredientStack lapis = new IngredientStack(Ingredient.of(Tags.Items.GEMS_LAPIS));
                makeGateAssembly(writer, 20_000, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.IRON);
                makeGateAssembly(writer, 40_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER,
                    EnumRedstoneChipset.IRON, IngredientStack.of(Blocks.NETHER_BRICKS));
                makeGateAssembly(writer, 80_000, EnumGateMaterial.GOLD, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.GOLD);

                makeGateModifierAssembly(writer, 40_000, EnumGateMaterial.IRON, EnumGateModifier.LAPIS, lapis);
                makeGateModifierAssembly(writer, 60_000, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ,
                    IngredientStack.of(EnumRedstoneChipset.QUARTZ.getStack()));
                makeGateModifierAssembly(writer, 80_000, EnumGateMaterial.IRON, EnumGateModifier.DIAMOND,
                    IngredientStack.of(EnumRedstoneChipset.DIAMOND.getStack()));

                makeGateModifierAssembly(writer, 80_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.LAPIS, lapis);
                makeGateModifierAssembly(writer, 100_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.QUARTZ,
                    IngredientStack.of(EnumRedstoneChipset.QUARTZ.getStack()));
                makeGateModifierAssembly(writer, 120_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.DIAMOND,
                    IngredientStack.of(EnumRedstoneChipset.DIAMOND.getStack()));

                makeGateModifierAssembly(writer, 100_000, EnumGateMaterial.GOLD, EnumGateModifier.LAPIS, lapis);
                makeGateModifierAssembly(writer, 140_000, EnumGateMaterial.GOLD, EnumGateModifier.QUARTZ,
                    IngredientStack.of(EnumRedstoneChipset.QUARTZ.getStack()));
                makeGateModifierAssembly(writer, 180_000, EnumGateMaterial.GOLD, EnumGateModifier.DIAMOND,
                    IngredientStack.of(EnumRedstoneChipset.DIAMOND.getStack()));
            }
		}
		if (BCSiliconItems.PLUG_LIGHT_SENSOR_ITEM.get() != null) {
           new AssemblyRecipeBuilder(500 * MjAPI.MJ, ImmutableSet.of(IngredientStack.of(Blocks.DAYLIGHT_DETECTOR)),
                new ItemStack(BCSiliconItems.PLUG_LIGHT_SENSOR_ITEM.get()))
           .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
           .save(writer, new ResourceLocation("buildcraftsilicon", "assembly/light_sensor"));
        }

        if (BCSiliconItems.PLUG_FACADE_ITEM.get() != null) {
        	SpecialRecipeBuilder.special(BCSiliconRecipes.FACADE_SERIALIZER.get()).save(writer, "buildcraftsilicon:special/facade");
            //ForgeRegistries.RECIPE_TYPES.register(FacadeSwapRecipe.INSTANCE);
        }

        if (BCSiliconItems.PLUG_LENS_ITEM.get() != null) {
            for (DyeColor colour : ColourUtil.COLOURS) {
                String name = String.format("assembly/lens/regular/%s", colour.getSerializedName());
                IngredientStack stainedGlass = IngredientStack.of(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(colour.getName()+"_stained_glass")));
                ImmutableSet<IngredientStack> input = ImmutableSet.of(stainedGlass);
                ItemStack output = BCSiliconItems.PLUG_LENS_ITEM.get().getStack(colour, false);
                new AssemblyRecipeBuilder(500 * MjAPI.MJ, input, output)
                .group("buildcraftsilicon:lens_regulars")
                .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
                .save(writer, new ResourceLocation("buildcraftsilicon",name));

                name = String.format("assembly/lens/filter/%s", colour.getSerializedName());
                output = BCSiliconItems.PLUG_LENS_ITEM.get().getStack(colour, true);
                input = ImmutableSet.of(stainedGlass, IngredientStack.of(Blocks.IRON_BARS));
                new AssemblyRecipeBuilder(500 * MjAPI.MJ, input, output)
                .group("buildcraftsilicon:lens_filters")
                .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
                .save(writer, new ResourceLocation("buildcraftsilicon",name));
            }

            IngredientStack glass = IngredientStack.of(Tags.Blocks.GLASS);
            ImmutableSet<IngredientStack> input = ImmutableSet.of(glass);
            ItemStack output = BCSiliconItems.PLUG_LENS_ITEM.get().getStack(null, false);
            new AssemblyRecipeBuilder(500 * MjAPI.MJ, input, output)
            .group("buildcraftsilicon:lens_regulars")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon", "assembly/lens/lens_regular"));

            output = BCSiliconItems.PLUG_LENS_ITEM.get().getStack(null, true);
            input = ImmutableSet.of(glass, IngredientStack.of(Blocks.IRON_BARS));
            new AssemblyRecipeBuilder(500 * MjAPI.MJ, input, output)
            .group("buildcraftsilicon:lens_regulars")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon", "assembly/lens/lens_filter"));
        }

        if (!BCSiliconItems.REDSTONE_CHIPSET_ITEMS.isEmpty()) {
            ImmutableSet<IngredientStack> input = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE));
            ItemStack output = EnumRedstoneChipset.RED.getStack(1);//TODO
            new AssemblyRecipeBuilder(10000 * MjAPI.MJ, input, output)
            .group("buildcraftsilicon:chipsets")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon","assembly/redstone_chipset"));

            input = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(Tags.Items.INGOTS_IRON));
            output = EnumRedstoneChipset.IRON.getStack(1);
            new AssemblyRecipeBuilder(20000 * MjAPI.MJ, input, output)
            .group("buildcraftsilicon:chipsets")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon","assembly/iron_chipset"));

            input = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(Tags.Items.INGOTS_GOLD));
            output = EnumRedstoneChipset.GOLD.getStack(1);
            new AssemblyRecipeBuilder(40000 * MjAPI.MJ, input, output)
            .group("buildcraftsilicon:chipsets")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon","assembly/gold_chipset"));

            input = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(Tags.Items.GEMS_QUARTZ));
            output = EnumRedstoneChipset.QUARTZ.getStack(1);
            new AssemblyRecipeBuilder(60000 * MjAPI.MJ, input, output)
            .group("buildcraftsilicon:chipsets")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon","assembly/quartz_chipset"));

            input = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(Tags.Items.GEMS_DIAMOND));
            output = EnumRedstoneChipset.DIAMOND.getStack(1);
            new AssemblyRecipeBuilder(80000 * MjAPI.MJ, input, output)
            .group("buildcraftsilicon:chipsets")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon","assembly/diamond_chipset"));
        }

        if (BCSiliconItems.GATE_COPIER_ITEM.isPresent()) {
            ImmutableSet.Builder<IngredientStack> input = ImmutableSet.builder();
            if (BCCoreItems.WRENCH.isPresent()) {
                input.add(IngredientStack.of(BCCoreItems.WRENCH.get()));
            } else {
                input.add(IngredientStack.of(Tags.Items.RODS_WOODEN));
                input.add(IngredientStack.of(Tags.Items.INGOTS_IRON));
            }

            if (!BCSiliconItems.REDSTONE_CHIPSET_ITEMS.isEmpty()) {
                input.add(IngredientStack.of(EnumRedstoneChipset.IRON.getStack(1)));
            } else {
                input.add(IngredientStack.of(Tags.Items.DUSTS_REDSTONE));
                input.add(IngredientStack.of(Tags.Items.DUSTS_REDSTONE));
                input.add(IngredientStack.of(Tags.Items.INGOTS_GOLD));
            }

            new AssemblyRecipeBuilder(500 * MjAPI.MJ, input.build(), new ItemStack(BCSiliconItems.GATE_COPIER_ITEM.get()))
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon","assembly/gate_copier"));
        }
	}
	
	private void makeGateRecipe0(Consumer<FinishedRecipe> writer, Ingredient m, EnumGateMaterial material,
			EnumGateModifier modifier) {
        GateVariant variant = new GateVariant(EnumGateLogic.AND, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER);
        ItemStack ironGateBase = BCSiliconItems.PLUG_GATE_ITEM.get().getStack(variant);
        NbtShapedRecipeBuilder builder = new NbtShapedRecipeBuilder(BCSiliconItems.PLUG_GATE_ITEM.get());
        builder
        .pattern(" m ")
        .pattern("mgm")
        .pattern(" m ")
        .define('g', StrictNBTIngredient.of(ironGateBase))
        .define('m', m)
        .group("buildcraftsilicon:iron_pluggate")
        .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()));
        builder.getTag().put("gate", variant.writeToNBT());
        builder.save(writer, new ResourceLocation("buildcraftsilicon:plug_gate_create/" + material.toString().toLowerCase() + "_" + modifier.toString().toLowerCase()));
		
	}

	private void makeGateRecipe(Consumer<FinishedRecipe> writer, Ingredient m, EnumGateMaterial material,
	        EnumGateModifier modifier) {
        GateVariant variant = new GateVariant(EnumGateLogic.AND, material, modifier);
        NbtShapedRecipeBuilder builder = new NbtShapedRecipeBuilder(BCSiliconItems.PLUG_GATE_ITEM.get());
		builder
        .pattern(" m ")
        .pattern("mrm")
        .pattern(" b ")
        .define('r', Tags.Items.DUSTS_REDSTONE)
        .define('b', BCTransportItems.plugBlocker.isPresent() ? BCTransportItems.plugBlocker.get() : Blocks.COBBLESTONE)
        .define('m', m)
        .group("buildcraftsilicon:basic_pluggate")
        .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()));
        builder.getTag().put("gate", variant.writeToNBT());
        builder.save(writer, new ResourceLocation("buildcraftsilicon:plug_gate_create/" + material.toString().toLowerCase() + "_" + modifier.toString().toLowerCase()));
       // builder.registerNbtAware("buildcraftsilicon:plug_gate_create_" + material + "_" + modifier);
	}
	
    private static void makeGateAssembly(Consumer<FinishedRecipe> writer, int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
            EnumRedstoneChipset chipset, IngredientStack... additional) {
            ImmutableSet.Builder<IngredientStack> temp = ImmutableSet.builder();
            temp.add(new IngredientStack(StrictNBTIngredient.of(chipset.getStack())));
            temp.add(additional);
            ImmutableSet<IngredientStack> input = temp.build();

            String name = String.format("assembly/gate/and_%s_%s", material.toString().toLowerCase(), modifier.toString().toLowerCase());
            ItemStack output = BCSiliconItems.PLUG_GATE_ITEM.get().getStack(new GateVariant(EnumGateLogic.AND, material, modifier));
            AssemblyRecipeBuilder andBuilder = new AssemblyRecipeBuilder(MjAPI.MJ * multiplier, input, output);
            andBuilder.group("buildcraftsilicon:gate_and")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon", name));

            name = String.format("assembly/gate/or_%s_%s", material.toString().toLowerCase(), modifier.toString().toLowerCase());
            output = BCSiliconItems.PLUG_GATE_ITEM.get().getStack(new GateVariant(EnumGateLogic.OR, material, modifier));
            AssemblyRecipeBuilder orBuilder = new AssemblyRecipeBuilder(MjAPI.MJ * multiplier, input, output);
            orBuilder.group("buildcraftsilicon:gate_or")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon", name));
    }
	
	private static void makeGateModifierAssembly(Consumer<FinishedRecipe> writer, int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
			IngredientStack... mods) {
		for (EnumGateLogic logic : EnumGateLogic.VALUES) {
			String name = String.format("assembly/gate/modifier/%s_%s_%s", logic.toString().toLowerCase(), material.toString().toLowerCase(), modifier.toString().toLowerCase());
			GateVariant variantFrom = new GateVariant(logic, material, EnumGateModifier.NO_MODIFIER);
			ItemStack toUpgrade = BCSiliconItems.PLUG_GATE_ITEM.get().getStack(variantFrom);
			ItemStack output = BCSiliconItems.PLUG_GATE_ITEM.get().getStack(new GateVariant(logic, material, modifier));
			Builder<IngredientStack> inputBuilder = new ImmutableSet.Builder<>();
			inputBuilder.add(new IngredientStack(StrictNBTIngredient.of(toUpgrade)));
			inputBuilder.add(mods);
			ImmutableSet<IngredientStack> input = inputBuilder.build();
			AssemblyRecipeBuilder builder = new AssemblyRecipeBuilder(MjAPI.MJ * multiplier, input, output);
			builder.group("buildcraftsilicon:gate_modifier")
            .unlockedBy("has_"+BCSiliconItems.ASSEMBLY_TABLE_ITEM.get().getDescriptionId(), TriggerInstance.hasItems(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get()))
            .save(writer, new ResourceLocation("buildcraftsilicon", name));
		}
	}
}
