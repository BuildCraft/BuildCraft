/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
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
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.IngredientStack;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.IngredientNBTBC;
import buildcraft.lib.recipe.RecipeBuilderShaped;

import buildcraft.core.BCCoreBlocks;
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

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        addPipeRecipe(BCTransportItems.pipeItemWood, "plankWood");
        addPipeRecipe(BCTransportItems.pipeItemCobble, "cobblestone");
        addPipeRecipe(BCTransportItems.pipeItemStone, "stone");
        addPipeRecipe(BCTransportItems.pipeItemQuartz, "blockQuartz");
        addPipeRecipe(BCTransportItems.pipeItemIron, "ingotIron");
        addPipeRecipe(BCTransportItems.pipeItemGold, "ingotGold");
        addPipeRecipe(BCTransportItems.pipeItemClay, Blocks.CLAY);
        addPipeRecipe(BCTransportItems.pipeItemSandstone,
            new ItemStack(Blocks.SANDSTONE, 1, OreDictionary.WILDCARD_VALUE));
        addPipeRecipe(BCTransportItems.pipeItemVoid, new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()),
            "dustRedstone");
        addPipeRecipe(BCTransportItems.pipeItemObsidian, Blocks.OBSIDIAN);
        addPipeRecipe(BCTransportItems.pipeItemDiamond, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemLapis, Blocks.LAPIS_BLOCK);
        addPipeRecipe(BCTransportItems.pipeItemDaizuli, Blocks.LAPIS_BLOCK, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemDiaWood, "plankWood", Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemStripes, "gearGold");
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeItemEmzuli, Blocks.LAPIS_BLOCK);

        Item waterproof = BCTransportItems.waterproof;
        if (waterproof == null) {
            waterproof = Items.SLIME_BALL;
        }
        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipeFluidWood, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipeFluidCobble, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipeFluidStone, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipeFluidQuartz, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipeFluidIron, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipeFluidGold, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemClay, BCTransportItems.pipeFluidClay, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipeFluidSandstone, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemVoid, BCTransportItems.pipeFluidVoid, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemObsidian, BCTransportItems.pipeFluidObsidian, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipeFluidDiamond, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeFluidDiaWood, waterproof);

        String upgrade = "dustRedstone";
        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipePowerWood, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipePowerCobble, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipePowerStone, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipePowerQuartz, upgrade);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipePowerIron, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipePowerGold, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipePowerSandstone, upgrade);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipePowerDiamond, upgrade);
        if (BCTransportItems.plugGate != null) {
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
            ItemStack ironGateBase = BCTransportItems.plugGate.getStack(variant);
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
                    ItemStack resultAnd = BCTransportItems.plugGate.getStack(varAnd);

                    GateVariant varOr = new GateVariant(EnumGateLogic.OR, material, modifier);
                    ItemStack resultOr = BCTransportItems.plugGate.getStack(varOr);

                    String regNamePrefix = resultOr.getItem().getRegistryName() + "_" + modifier + "_" + material;
                    ForgeRegistries.RECIPES.register(new ShapedOreRecipe(resultOr.getItem().getRegistryName(), resultAnd, "i", 'i', new IngredientNBTBC(resultOr)).setRegistryName(regNamePrefix + "_or"));
                    ForgeRegistries.RECIPES.register(new ShapedOreRecipe(resultAnd.getItem().getRegistryName(), resultOr, "i", 'i', new IngredientNBTBC(resultAnd)).setRegistryName(regNamePrefix + "_and"));
                }
            }
        }

        if (BCTransportItems.plugPulsar != null) {
            ItemStack output = new ItemStack(BCTransportItems.plugPulsar);

            ItemStack redstoneEngine;
            if (BCCoreBlocks.engine != null) {
                redstoneEngine = BCCoreBlocks.engine.getStack(EnumEngineType.WOOD);
            } else {
                redstoneEngine = new ItemStack(Blocks.REDSTONE_BLOCK);
            }

            if (SILICON_TABLE_ASSEMBLY != null) {
                Set<IngredientStack> input = new HashSet<>();
                input.add(new IngredientStack(Ingredient.fromStacks(redstoneEngine)));
                input.add(new IngredientStack(CraftingHelper.getIngredient("ingotIron"), 2));
                AssemblyRecipe recipe = new AssemblyRecipeBasic("plug_pulsar", 1000 * MjAPI.MJ, input, output);
                AssemblyRecipeRegistry.register(recipe);
            }
        }
        if (BCTransportItems.plugGate != null) {
            IngredientStack lapis = IngredientStack.of("gemLapis");
            makeGateAssembly(20_000, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.IRON);
            makeGateAssembly(40_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER,
                EnumRedstoneChipset.IRON, IngredientStack.of(new ItemStack(Blocks.NETHER_BRICK)));
            makeGateAssembly(80_000, EnumGateMaterial.GOLD, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.GOLD);

            makeGateModifierAssembly(40_000, EnumGateMaterial.IRON, EnumGateModifier.LAPIS, lapis);
            makeGateModifierAssembly(60_000, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ,
                IngredientStack.of(EnumRedstoneChipset.QUARTZ.getStack()));
            makeGateModifierAssembly(80_000, EnumGateMaterial.IRON, EnumGateModifier.DIAMOND,
                IngredientStack.of(EnumRedstoneChipset.DIAMOND.getStack()));

            makeGateModifierAssembly(80_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.LAPIS, lapis);
            makeGateModifierAssembly(100_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.QUARTZ,
                IngredientStack.of(EnumRedstoneChipset.QUARTZ.getStack()));
            makeGateModifierAssembly(120_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.DIAMOND,
                IngredientStack.of(EnumRedstoneChipset.DIAMOND.getStack()));

            makeGateModifierAssembly(100_000, EnumGateMaterial.GOLD, EnumGateModifier.LAPIS, lapis);
            makeGateModifierAssembly(140_000, EnumGateMaterial.GOLD, EnumGateModifier.QUARTZ,
                IngredientStack.of(EnumRedstoneChipset.QUARTZ.getStack()));
            makeGateModifierAssembly(180_000, EnumGateMaterial.GOLD, EnumGateModifier.DIAMOND,
                IngredientStack.of(EnumRedstoneChipset.DIAMOND.getStack()));
        }

        if (BCTransportItems.plugLightSensor != null) {
            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("light-sensor", 500 * MjAPI.MJ,
                ImmutableSet.of(IngredientStack.of(Blocks.DAYLIGHT_DETECTOR)),
                new ItemStack(BCTransportItems.plugLightSensor)));
        }

        if (BCTransportItems.plugFacade != null) {
            AssemblyRecipeRegistry.register(FacadeAssemblyRecipes.INSTANCE);
            ForgeRegistries.RECIPES.register(FacadeSwapRecipe.INSTANCE);
        }

        if (BCTransportItems.wire != null) {
            for (EnumDyeColor color : ColourUtil.COLOURS) {
                String name = String.format("wire-%s", color.getUnlocalizedName());
                ImmutableSet<IngredientStack> input = ImmutableSet.of(IngredientStack.of("dustRedstone"),
                    IngredientStack.of(ColourUtil.getDyeName(color)));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 10_000 * MjAPI.MJ, input,
                    new ItemStack(BCTransportItems.wire, 8, color.getMetadata())));
            }
        }

        if (BCTransportItems.plugLens != null) {
            for (EnumDyeColor colour : ColourUtil.COLOURS) {
                String name = String.format("lens-regular-%s", colour.getUnlocalizedName());
                IngredientStack stainedGlass = IngredientStack.of("blockGlass" + ColourUtil.getName(colour));
                ImmutableSet<IngredientStack> input = ImmutableSet.of(stainedGlass);
                ItemStack output = BCTransportItems.plugLens.getStack(colour, false);
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));

                name = String.format("lens-filter-%s", colour.getUnlocalizedName());
                output = BCTransportItems.plugLens.getStack(colour, true);
                input = ImmutableSet.of(stainedGlass, IngredientStack.of(new ItemStack(Blocks.IRON_BARS)));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));
            }

            IngredientStack glass = IngredientStack.of("blockGlass");
            ImmutableSet<IngredientStack> input = ImmutableSet.of(glass);
            ItemStack output = BCTransportItems.plugLens.getStack(null, false);
            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-regular", 500 * MjAPI.MJ, input, output));

            output = BCTransportItems.plugLens.getStack(null, true);
            input = ImmutableSet.of(glass, IngredientStack.of(new ItemStack(Blocks.IRON_BARS)));
            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-filter", 500 * MjAPI.MJ, input, output));
        }
    }


    private static void makeGateModifierAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
                                                 IngredientStack... mods) {
        for (EnumGateLogic logic : EnumGateLogic.VALUES) {
            String name = String.format("gate-modifier-%s-%s-%s", logic, material, modifier);
            ItemStack toUpgrade =
                BCTransportItems.plugGate.getStack(new GateVariant(logic, material, EnumGateModifier.NO_MODIFIER));
            ItemStack output = BCTransportItems.plugGate.getStack(new GateVariant(logic, material, modifier));
            ImmutableSet<IngredientStack> input = new ImmutableSet.Builder<IngredientStack>()
                .add(IngredientStack.of(toUpgrade)).add(mods).build();
            AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));
        }
    }


    private static void makeGateAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
                                         EnumRedstoneChipset chipset, IngredientStack... additional) {
        ImmutableSet.Builder<IngredientStack> temp = ImmutableSet.builder();
        temp.add(IngredientStack.of(chipset.getStack()));
        temp.add(additional);
        ImmutableSet<IngredientStack> input = temp.build();

        String name = String.format("gate-and-%s-%s", material, modifier);
        ItemStack output = BCTransportItems.plugGate.getStack(new GateVariant(EnumGateLogic.AND, material, modifier));
        AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));

        name = String.format("gate-or-%s-%s", material, modifier);
        output = BCTransportItems.plugGate.getStack(new GateVariant(EnumGateLogic.OR, material, modifier));
        AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));
    }

    private static void makeGateRecipe(RecipeBuilderShaped builder, EnumGateMaterial material,
                                       EnumGateModifier modifier) {
        GateVariant variant = new GateVariant(EnumGateLogic.AND, material, modifier);
        builder.setResult(BCTransportItems.plugGate.getStack(variant));
        builder.registerNbtAware("buildcrafttransport:plug_gate_create_" + material + "_" + modifier);
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
