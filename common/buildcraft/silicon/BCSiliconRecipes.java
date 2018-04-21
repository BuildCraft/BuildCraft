/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import buildcraft.api.core.BCLog;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.StackDefinition;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.inventory.filter.OreStackFilter;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.lib.recipe.NBTAwareShapedOreRecipe;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import buildcraft.api.BCItems;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.IngredientStack;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.IngredientNBTBC;
import buildcraft.lib.recipe.IntegrationRecipeBasic;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.lib.recipe.RecipeBuilderShaped;

import buildcraft.core.BCCoreBlocks;
import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.recipe.FacadeAssemblyRecipes;
import buildcraft.silicon.recipe.FacadeSwapRecipe;
import buildcraft.transport.BCTransportItems;

@Mod.EventBusSubscriber(modid = BCSilicon.MODID)
public class BCSiliconRecipes {
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void init() {
        //scanForJsonRecipes();
        if (BCSiliconItems.plugGate != null) {
            // You can craft some of the basic gate types in a normal crafting table
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add(" m ");
            builder.add("mrm");
            builder.add(" b ");
            builder.map('r', "dustRedstone");
            builder.map('b', BCTransportItems.plugBlocker, Blocks.COBBLESTONE);

            // Base craftable types

            builder.map('m', Items.BRICK);
            makeGateRecipe(builder, EnumGateMaterial.CLAY_BRICK, EnumGateModifier.NO_MODIFIER);

            builder.map('m', "ingotIron");
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER);

            builder.map('m', Items.NETHERBRICK);
            makeGateRecipe(builder, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER);

            // Iron modifier addition
            GateVariant variant =
                    new GateVariant(EnumGateLogic.AND, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER);
            ItemStack ironGateBase = BCSiliconItems.plugGate.getStack(variant);
            builder = new RecipeBuilderShaped();
            builder.add(" m ");
            builder.add("mgm");
            builder.add(" m ");
            builder.map('g', ironGateBase);

            builder.map('m', new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()));
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.LAPIS);

            builder.map('m', Items.QUARTZ);
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ);

            // And Gate <-> Or Gate (shapeless)
            // TODO: Create a recipe class for this instead!
            for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
                if (material == EnumGateMaterial.CLAY_BRICK) {
                    continue;
                }
                for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                    GateVariant varAnd = new GateVariant(EnumGateLogic.AND, material, modifier);
                    ItemStack resultAnd = BCSiliconItems.plugGate.getStack(varAnd);

                    GateVariant varOr = new GateVariant(EnumGateLogic.OR, material, modifier);
                    ItemStack resultOr = BCSiliconItems.plugGate.getStack(varOr);

                    GameRegistry.addRecipe(new NBTAwareShapedOreRecipe(resultAnd, "i", 'i', resultOr));
                    GameRegistry.addRecipe(new NBTAwareShapedOreRecipe(resultOr, "i", 'i', resultAnd));
                }
            }
        }

        if (BCSiliconItems.redstoneChipset != null) {
            ItemStack output = new ItemStack(BCSiliconItems.redstoneChipset, 1, 4);
            Set<StackDefinition> input = new HashSet<>();
            input.add(OreStackFilter.definition(1, "dustRedstone"));
            input.add(OreStackFilter.definition(1, "gemDiamond"));
            AssemblyRecipe recipe = new AssemblyRecipeBasic("diamond_chipset", 80000 * MjAPI.MJ, input, output);
            AssemblyRecipeRegistry.register(recipe);

            input.clear();
            output = new ItemStack(BCSiliconItems.redstoneChipset, 1, 3);
            input.add(OreStackFilter.definition(1, "dustRedstone"));
            input.add(OreStackFilter.definition(1, "gemQuartz"));
            recipe = new AssemblyRecipeBasic("quartz_chipset", 60000 * MjAPI.MJ, input, output);
            AssemblyRecipeRegistry.register(recipe);

            input.clear();
            output = new ItemStack(BCSiliconItems.redstoneChipset, 1, 2);
            input.add(OreStackFilter.definition(1, "dustRedstone"));
            input.add(OreStackFilter.definition(1, "ingotGold"));
            recipe = new AssemblyRecipeBasic("gold_chipset", 40000 * MjAPI.MJ, input, output);
            AssemblyRecipeRegistry.register(recipe);

            input.clear();
            output = new ItemStack(BCSiliconItems.redstoneChipset, 1, 1);
            input.add(OreStackFilter.definition(1, "dustRedstone"));
            input.add(OreStackFilter.definition(1, "ingotIron"));
            recipe = new AssemblyRecipeBasic("iron_chipset", 20000 * MjAPI.MJ, input, output);
            AssemblyRecipeRegistry.register(recipe);

            input.clear();
            output = new ItemStack(BCSiliconItems.redstoneChipset, 1, 0);
            input.add(OreStackFilter.definition(1, "dustRedstone"));
            recipe = new AssemblyRecipeBasic("redstone_chipset", 10000 * MjAPI.MJ, input, output);
            AssemblyRecipeRegistry.register(recipe);
        }


        if (BCSiliconItems.plugPulsar != null) {
            ItemStack output = new ItemStack(BCSiliconItems.plugPulsar);

            ItemStack redstoneEngine;
            if (BCCoreBlocks.engine != null) {
                redstoneEngine = BCCoreBlocks.engine.getStack(EnumEngineType.WOOD);
            } else {
                redstoneEngine = new ItemStack(Blocks.REDSTONE_BLOCK);
            }

            Set<StackDefinition > input = new HashSet<>();
            input.add(ArrayStackFilter.definition(redstoneEngine));
            input.add(OreStackFilter.definition(2, "ingotIron"));
            AssemblyRecipe recipe = new AssemblyRecipeBasic("plug_pulsar", 1000 * MjAPI.MJ, input, output);
            AssemblyRecipeRegistry.register(recipe);
        }
        if (BCSiliconItems.plugGate != null) {
            StackDefinition lapis = OreStackFilter.definition("gemLapis");
            makeGateAssembly(20_000, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.IRON);
            makeGateAssembly(40_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER,
                    EnumRedstoneChipset.IRON, ArrayStackFilter.definition(new ItemStack(Blocks.NETHER_BRICK)));
            makeGateAssembly(80_000, EnumGateMaterial.GOLD, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.GOLD);

            makeGateModifierAssembly(40_000, EnumGateMaterial.IRON, EnumGateModifier.LAPIS, lapis);
            makeGateModifierAssembly(60_000, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ,
                    ArrayStackFilter.definition(EnumRedstoneChipset.QUARTZ.getStack()));
            makeGateModifierAssembly(80_000, EnumGateMaterial.IRON, EnumGateModifier.DIAMOND,
                    ArrayStackFilter.definition(EnumRedstoneChipset.DIAMOND.getStack()));

            makeGateModifierAssembly(80_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.LAPIS, lapis);
            makeGateModifierAssembly(100_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.QUARTZ,
                    ArrayStackFilter.definition(EnumRedstoneChipset.QUARTZ.getStack()));
            makeGateModifierAssembly(120_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.DIAMOND,
                    ArrayStackFilter.definition(EnumRedstoneChipset.DIAMOND.getStack()));

            makeGateModifierAssembly(100_000, EnumGateMaterial.GOLD, EnumGateModifier.LAPIS, lapis);
            makeGateModifierAssembly(140_000, EnumGateMaterial.GOLD, EnumGateModifier.QUARTZ,
                    ArrayStackFilter.definition(EnumRedstoneChipset.QUARTZ.getStack()));
            makeGateModifierAssembly(180_000, EnumGateMaterial.GOLD, EnumGateModifier.DIAMOND,
                    ArrayStackFilter.definition(EnumRedstoneChipset.DIAMOND.getStack()));
        }

        if (BCSiliconItems.plugLightSensor != null) {
            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("light-sensor", 500 * MjAPI.MJ,
                    ImmutableSet.of(ArrayStackFilter.definition(Blocks.DAYLIGHT_DETECTOR)),
                    new ItemStack(BCSiliconItems.plugLightSensor)));
        }

        if (BCSiliconItems.plugFacade != null) {
            AssemblyRecipeRegistry.register(FacadeAssemblyRecipes.INSTANCE);
            GameRegistry.addRecipe(FacadeSwapRecipe.INSTANCE);
        }

        if (BCSiliconItems.plugLens != null) {
            for (EnumDyeColor colour : ColourUtil.COLOURS) {
                String name = String.format("lens-regular-%s", colour.getUnlocalizedName());
                StackDefinition stainedGlass = OreStackFilter.definition("blockGlass" + ColourUtil.getName(colour));
                ImmutableSet<StackDefinition> input = ImmutableSet.of(stainedGlass);
                ItemStack output = BCSiliconItems.plugLens.getStack(colour, false);
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));

                name = String.format("lens-filter-%s", colour.getUnlocalizedName());
                output = BCSiliconItems.plugLens.getStack(colour, true);
                input = ImmutableSet.of(stainedGlass, ArrayStackFilter.definition(new ItemStack(Blocks.IRON_BARS)));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));
            }

            StackDefinition glass = OreStackFilter.definition("blockGlass");
            ImmutableSet<StackDefinition> input = ImmutableSet.of(glass);
            ItemStack output = BCSiliconItems.plugLens.getStack(null, false);
            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-regular", 500 * MjAPI.MJ, input, output));

            output = BCSiliconItems.plugLens.getStack(null, true);
            input = ImmutableSet.of(glass, ArrayStackFilter.definition(new ItemStack(Blocks.IRON_BARS)));
            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-filter", 500 * MjAPI.MJ, input, output));
        }
    }

    private static void makeGateModifierAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
                                                 StackDefinition... mods) {
        for (EnumGateLogic logic : EnumGateLogic.VALUES) {
            String name = String.format("gate-modifier-%s-%s-%s", logic, material, modifier);
            GateVariant variantFrom = new GateVariant(logic, material, EnumGateModifier.NO_MODIFIER);
            ItemStack toUpgrade = BCSiliconItems.plugGate.getStack(variantFrom);
            ItemStack output = BCSiliconItems.plugGate.getStack(new GateVariant(logic, material, modifier));
            ImmutableSet.Builder<StackDefinition> inputBuilder = new ImmutableSet.Builder<>();
            inputBuilder.add(ArrayStackFilter.definition(toUpgrade));
            inputBuilder.add(mods);
            ImmutableSet<StackDefinition> input = inputBuilder.build();
            AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));
        }
    }

    private static void makeGateAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
                                         EnumRedstoneChipset chipset, StackDefinition... additional) {
        ImmutableSet.Builder<StackDefinition> temp = ImmutableSet.builder();
        temp.add(ArrayStackFilter.definition(chipset.getStack()));
        temp.add(additional);
        ImmutableSet<StackDefinition> input = temp.build();

        String name = String.format("gate-and-%s-%s", material, modifier);
        ItemStack output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.AND, material, modifier));
        AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));

        name = String.format("gate-or-%s-%s", material, modifier);
        output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.OR, material, modifier));
        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output));
    }

    private static void makeGateRecipe(RecipeBuilderShaped builder, EnumGateMaterial material,
                                       EnumGateModifier modifier) {
        GateVariant variant = new GateVariant(EnumGateLogic.AND, material, modifier);
        builder.setResult(BCSiliconItems.plugGate.getStack(variant));
        builder.registerNbtAware();
    }

}