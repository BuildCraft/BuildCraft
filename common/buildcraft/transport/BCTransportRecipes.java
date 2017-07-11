/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.StackDefinition;

import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.inventory.filter.OreStackFilter;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.NBTAwareShapedOreRecipe;
import buildcraft.lib.recipe.RecipeBuilderShaped;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreItems;
import buildcraft.transport.gate.EnumGateLogic;
import buildcraft.transport.gate.EnumGateMaterial;
import buildcraft.transport.gate.EnumGateModifier;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.item.ItemPipeHolder;
import buildcraft.transport.recipe.FacadeAssemblyRecipes;
import buildcraft.transport.recipe.FacadeSwapRecipe;

@Mod.EventBusSubscriber(modid = BCTransport.MODID)
public class BCTransportRecipes {
    @GameRegistry.ObjectHolder("buildcraftsilicon:assembly_table")
    private static final Block SILICON_TABLE_ASSEMBLY = null;
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        addPipeRecipe(BCTransportItems.PIPE_WOOD_ITEM, "plankWood");
        addPipeRecipe(BCTransportItems.PIPE_COBBLE_ITEM, "cobblestone");
        addPipeRecipe(BCTransportItems.PIPE_STONE_ITEM, "stone");
        addPipeRecipe(BCTransportItems.PIPE_QUARTZ_ITEM, "blockQuartz");
        addPipeRecipe(BCTransportItems.PIPE_IRON_ITEM, "ingotIron");
        addPipeRecipe(BCTransportItems.PIPE_GOLD_ITEM, "ingotGold");
        addPipeRecipe(BCTransportItems.PIPE_CLAY_ITEM, Blocks.CLAY);
        addPipeRecipe(BCTransportItems.PIPE_SANDSTONE_ITEM,
            new ItemStack(Blocks.SANDSTONE, 1, OreDictionary.WILDCARD_VALUE));
        addPipeRecipe(BCTransportItems.PIPE_VOID_ITEM, new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()),
            "dustRedstone");
        addPipeRecipe(BCTransportItems.PIPE_OBSIDIAN_ITEM, Blocks.OBSIDIAN);
        addPipeRecipe(BCTransportItems.PIPE_DIAMOND_ITEM, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.PIPE_LAPIS_ITEM, Blocks.LAPIS_BLOCK);
        addPipeRecipe(BCTransportItems.PIPE_DAIZULI_ITEM, Blocks.LAPIS_BLOCK, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.PIPE_DIAWOOD_ITEM, "plankWood", Items.DIAMOND);

        addPipeUpgradeRecipe(BCTransportItems.PIPE_WOOD_ITEM, BCTransportItems.PIPE_WOOD_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_COBBLE_ITEM, BCTransportItems.PIPE_COBBLE_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_STONE_ITEM, BCTransportItems.PIPE_STONE_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_QUARTZ_ITEM, BCTransportItems.PIPE_QUARTZ_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_IRON_ITEM, BCTransportItems.PIPE_IRON_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_GOLD_ITEM, BCTransportItems.PIPE_GOLD_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_CLAY_ITEM, BCTransportItems.PIPE_CLAY_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_SANDSTONE_ITEM, BCTransportItems.PIPE_SANDSTONE_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_VOID_ITEM, BCTransportItems.PIPE_VOID_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_OBSIDIAN_ITEM, BCTransportItems.PIPE_OBSIDIAN_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_DIAMOND_ITEM, BCTransportItems.PIPE_DIAMOND_FLUID, BCTransportItems.WATERPROOF);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_DIAWOOD_ITEM, BCTransportItems.PIPE_DIAWOOD_FLUID, BCTransportItems.WATERPROOF);

        String upgrade = "dustRedstone";
        addPipeUpgradeRecipe(BCTransportItems.PIPE_WOOD_ITEM, BCTransportItems.PIPE_WOOD_POWER, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_COBBLE_ITEM, BCTransportItems.PIPE_COBBLE_POWER, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_STONE_ITEM, BCTransportItems.PIPE_STONE_POWER, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_QUARTZ_ITEM, BCTransportItems.PIPE_QUARTZ_POWER, upgrade);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipePowerIron, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_GOLD_ITEM, BCTransportItems.PIPE_GOLD_POWER, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.PIPE_SANDSTONE_ITEM, BCTransportItems.PIPE_SANDSTONE_POWER, upgrade);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipePowerDiamond, upgrade);
        {
            ItemStack output = new ItemStack(BCTransportItems.PLUG_PULSAR);

            ItemStack redstoneEngine;
            if (BCCoreBlocks.ENGINE != null) {
                redstoneEngine = BCCoreBlocks.ENGINE.getStack(EnumEngineType.WOOD);
            } else {
                redstoneEngine = new ItemStack(Blocks.REDSTONE_BLOCK);
            }

            if (SILICON_TABLE_ASSEMBLY != null) {
                Set<StackDefinition> input = new HashSet<>();
                input.add(ArrayStackFilter.definition(redstoneEngine));
                input.add(OreStackFilter.definition(2, "ingotIron"));
                AssemblyRecipe recipe = new AssemblyRecipe("plug_pulsar", 1000 * MjAPI.MJ, input, output);
                AssemblyRecipeRegistry.INSTANCE.addRecipe(recipe);
            }

        }
        if (BCTransportItems.PLUG_GATE != null) {
            // You can craft some of the basic gate types in a normal crafting table
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add(" m ");
            builder.add("mrm");
            builder.add(" b ");
            builder.map('r', "dustRedstone");
            builder.map('b', BCTransportItems.PLUG_BLOCKER, Blocks.COBBLESTONE);

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
            ItemStack ironGateBase = BCTransportItems.PLUG_GATE.getStack(variant);
            builder = new RecipeBuilderShaped();
            builder.add(" m ");
            builder.add("mgm");
            builder.add(" m ");
            builder.map('g', ironGateBase);

            builder.map('m', new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()));
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.LAPIS);

            builder.map('m', Items.QUARTZ);
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ);

            if (BCCoreItems.DIAMOND_SHARD != null) {
                builder.map('m', BCCoreItems.DIAMOND_SHARD);
                makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.DIAMOND);
            }

            // And Gate <-> Or Gate (shapeless)
            for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
                if (material == EnumGateMaterial.CLAY_BRICK) {
                    continue;
                }
                for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                    GateVariant varAnd = new GateVariant(EnumGateLogic.AND, material, modifier);
                    ItemStack resultAnd = BCTransportItems.PLUG_GATE.getStack(varAnd);

                    GateVariant varOr = new GateVariant(EnumGateLogic.OR, material, modifier);
                    ItemStack resultOr = BCTransportItems.PLUG_GATE.getStack(varOr);

                    ForgeRegistries.RECIPES.register(new NBTAwareShapedOreRecipe(resultAnd, "i", 'i', resultOr).setRegistryName(resultOr.getItem().getRegistryName() + "_" + modifier));
                    ForgeRegistries.RECIPES.register(new NBTAwareShapedOreRecipe(resultOr, "i", 'i', resultAnd).setRegistryName(resultAnd.getItem().getRegistryName() + "_" + modifier));
                }
            }
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

            if (BCTransportItems.WIRE != null) {
                for (EnumDyeColor color : ColourUtil.COLOURS) {
                    String name = String.format("wire-%s", color.getUnlocalizedName());
                    ImmutableSet<StackDefinition> input = ImmutableSet.of(OreStackFilter.definition("dustRedstone"),
                        OreStackFilter.definition(ColourUtil.getDyeName(color)));
                    AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, 10_000 * MjAPI.MJ, input,
                        new ItemStack(BCTransportItems.WIRE, 8, color.getMetadata())));
                }
            }

            if (BCTransportItems.PLUG_LENS != null) {
                for (EnumDyeColor colour : ColourUtil.COLOURS) {
                    String name = String.format("lens-regular-%s", colour.getUnlocalizedName());
                    StackDefinition stainedGlass = OreStackFilter.definition("blockGlass" + ColourUtil.getName(colour));
                    ImmutableSet<StackDefinition> input = ImmutableSet.of(stainedGlass);
                    ItemStack output = BCTransportItems.PLUG_LENS.getStack(colour, false);
                    AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, 500 * MjAPI.MJ, input, output));

                    name = String.format("lens-filter-%s", colour.getUnlocalizedName());
                    output = BCTransportItems.PLUG_LENS.getStack(colour, true);
                    input = ImmutableSet.of(stainedGlass, ArrayStackFilter.definition(new ItemStack(Blocks.IRON_BARS)));
                    AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, 500 * MjAPI.MJ, input, output));
                }

                StackDefinition glass = OreStackFilter.definition("blockGlass");
                ImmutableSet<StackDefinition> input = ImmutableSet.of(glass);
                ItemStack output = BCTransportItems.PLUG_LENS.getStack(null, false);
                AssemblyRecipeRegistry.INSTANCE
                    .addRecipe(new AssemblyRecipe("lens-regular", 500 * MjAPI.MJ, input, output));

                output = BCTransportItems.PLUG_LENS.getStack(null, true);
                input = ImmutableSet.of(glass, ArrayStackFilter.definition(new ItemStack(Blocks.IRON_BARS)));
                AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe("lens-filter", 500 * MjAPI.MJ, input, output));
            }

            if (BCTransportItems.PLUG_LIGHT_SENSOR != null) {
                BuildcraftRecipeRegistry.assemblyRecipes.addRecipe(new AssemblyRecipe("light-sensor", 500 * MjAPI.MJ,
                    ImmutableSet.of(ArrayStackFilter.definition(Blocks.DAYLIGHT_DETECTOR)),
                    new ItemStack(BCTransportItems.PLUG_LIGHT_SENSOR)));
            }

            if (BCTransportItems.PLUG_FACADE != null) {
                AssemblyRecipeRegistry.INSTANCE.addRecipeProvider(FacadeAssemblyRecipes.INSTANCE);
                RecipeSorter.register("buildcraftlib:facade_swap", FacadeSwapRecipe.class, RecipeSorter.Category.SHAPELESS,
                    "before:minecraft:shapeless");
                ForgeRegistries.RECIPES.register(FacadeSwapRecipe.INSTANCE);
            }
        }
    


    private static void makeGateModifierAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
                                                 StackDefinition... mods) {
        for (EnumGateLogic logic : EnumGateLogic.VALUES) {
            String name = String.format("gate-modifier-%s-%s-%s", logic, material, modifier);
            ItemStack toUpgrade =
                BCTransportItems.PLUG_GATE.getStack(new GateVariant(logic, material, EnumGateModifier.NO_MODIFIER));
            ItemStack output = BCTransportItems.PLUG_GATE.getStack(new GateVariant(logic, material, modifier));
            ImmutableSet<StackDefinition> input = new ImmutableSet.Builder<StackDefinition>()
                .add(ArrayStackFilter.definition(toUpgrade)).add(mods).build();
            AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, MjAPI.MJ * multiplier, input, output));
        }
    }

    private static void makeGateAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
                                         EnumRedstoneChipset chipset, StackDefinition... additional) {
        ImmutableSet.Builder<StackDefinition> temp = ImmutableSet.builder();
        temp.add(ArrayStackFilter.definition(chipset.getStack()));
        temp.add(additional);
        ImmutableSet<StackDefinition> input = temp.build();

        String name = String.format("gate-and-%s-%s", material, modifier);
        ItemStack output = BCTransportItems.PLUG_GATE.getStack(new GateVariant(EnumGateLogic.AND, material, modifier));
        AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, MjAPI.MJ * multiplier, input, output));

        name = String.format("gate-or-%s-%s", material, modifier);
        output = BCTransportItems.PLUG_GATE.getStack(new GateVariant(EnumGateLogic.OR, material, modifier));
        AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, MjAPI.MJ * multiplier, input, output));
    }

    private static void makeGateRecipe(RecipeBuilderShaped builder, EnumGateMaterial material,
                                       EnumGateModifier modifier) {
        GateVariant variant = new GateVariant(EnumGateLogic.AND, material, modifier);
        builder.setResult(BCTransportItems.PLUG_GATE.getStack(variant));
        builder.registerNbtAware();
    }

    private static void addPipeRecipe(ItemPipeHolder pipe, Object material) {
        addPipeRecipe(pipe, material, material);
    }

    private static void addPipeRecipe(ItemPipeHolder pipe, Object left, Object right) {
        if (pipe == null) {
            return;
        }
        ItemStack result = new ItemStack(pipe, 8);
        IRecipe recipe = new ShapedOreRecipe(pipe.getRegistryName(),
            result, "lgr", 'l', left, 'r', right, 'g', "blockGlassColorless");
        recipe.setRegistryName(new ResourceLocation(pipe.getRegistryName() + "_colorless"));
        ForgeRegistries.RECIPES.register(recipe);

        for (EnumDyeColor colour : EnumDyeColor.values()) {
            ItemStack resultStack = new ItemStack(pipe, 8, colour.getMetadata() + 1);
            IRecipe colorRecipe = new ShapedOreRecipe(pipe.getRegistryName(), resultStack,
                "lgr", 'l', left, 'r', right, 'g', "blockGlass" + ColourUtil.getName(colour));
            colorRecipe.setRegistryName(new ResourceLocation(pipe.getRegistryName() + "_" + colour));
            ForgeRegistries.RECIPES.register(colorRecipe);
        }


    }

    private static void addPipeUpgradeRecipe(ItemPipeHolder from, ItemPipeHolder to, Object additional) {
        if (from == null || to == null) {
            return;
        }
        if (additional == null) {
            throw new NullPointerException("additional");
        }

        IRecipe returnRecipe = new ShapelessOreRecipe(from.getRegistryName(), new ItemStack(from), new ItemStack(to)).setRegistryName(new ResourceLocation(from.getRegistryName() + "_undo"));
        ForgeRegistries.RECIPES.register(returnRecipe);

        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.fromItem(from));
        list.add(CraftingHelper.getIngredient(additional));

        IRecipe upgradeRecipe = new ShapelessRecipes(to.getRegistryName().getResourcePath(), new ItemStack(to), list).setRegistryName(new ResourceLocation(to.getRegistryName() + "_colorless"));
        ForgeRegistries.RECIPES.register(upgradeRecipe);

        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            ItemStack f = new ItemStack(from, 1, colour.getMetadata() + 1);
            ItemStack t = new ItemStack(to, 1, colour.getMetadata() + 1);
            IRecipe returnRecipeColored = new ShapelessOreRecipe(from.getRegistryName(), f, t).setRegistryName(new ResourceLocation(from.getRegistryName() + colour.getName() + "_undo"));
            ForgeRegistries.RECIPES.register(returnRecipeColored);

            NonNullList<Ingredient> colorList = NonNullList.create();
            colorList.add(Ingredient.fromStacks(f));
            colorList.add(CraftingHelper.getIngredient(additional));

            IRecipe upgradeRecipeColored = new ShapelessOreRecipe(to.getRegistryName(), colorList, t).setRegistryName(new ResourceLocation(to.getRegistryName() + "_" + colour.getName()));
            ForgeRegistries.RECIPES.register(upgradeRecipeColored);
        }
    }
}
