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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;

import buildcraft.api.BCItems;
import buildcraft.api.core.BCLog;
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

@Mod.EventBusSubscriber(modid = BCSilicon.MODID)
public class BCSiliconRecipes {
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        scanForJsonRecipes();
        if (BCSiliconItems.plugGate != null) {
            // You can craft some of the basic gate types in a normal crafting table
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add(" m ");
            builder.add("mrm");
            builder.add(" b ");
            builder.map('r', "dustRedstone");
            builder.map('b', BCItems.Transport.PLUG_BLOCKER, Blocks.COBBLESTONE);

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

                    String regNamePrefix = resultOr.getItem().getRegistryName() + "_" + modifier + "_" + material;
                    ForgeRegistries.RECIPES.register(new ShapedOreRecipe(resultOr.getItem().getRegistryName(),
                        resultAnd, "i", 'i', new IngredientNBTBC(resultOr)).setRegistryName(regNamePrefix + "_or"));
                    ForgeRegistries.RECIPES.register(new ShapedOreRecipe(resultAnd.getItem().getRegistryName(),
                        resultOr, "i", 'i', new IngredientNBTBC(resultAnd)).setRegistryName(regNamePrefix + "_and"));
                }
            }
        }

        if (BCSiliconItems.plugPulsar != null) {
            ItemStack output = new ItemStack(BCSiliconItems.plugPulsar);

            ItemStack redstoneEngine;
            if (BCCoreBlocks.engine != null) {
                redstoneEngine = BCCoreBlocks.engine.getStack(EnumEngineType.WOOD);
            } else {
                redstoneEngine = new ItemStack(Blocks.REDSTONE_BLOCK);
            }

            Set<IngredientStack> input = new HashSet<>();
            input.add(new IngredientStack(Ingredient.fromStacks(redstoneEngine)));
            input.add(new IngredientStack(CraftingHelper.getIngredient("ingotIron"), 2));
            AssemblyRecipe recipe = new AssemblyRecipeBasic("plug_pulsar", 1000 * MjAPI.MJ, input, output);
            AssemblyRecipeRegistry.register(recipe);
        }
        if (BCSiliconItems.plugGate != null) {
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

        if (BCSiliconItems.plugLightSensor != null) {
            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("light-sensor", 500 * MjAPI.MJ,
                ImmutableSet.of(IngredientStack.of(Blocks.DAYLIGHT_DETECTOR)),
                new ItemStack(BCSiliconItems.plugLightSensor)));
        }

        if (BCSiliconItems.plugFacade != null) {
            AssemblyRecipeRegistry.register(FacadeAssemblyRecipes.INSTANCE);
            ForgeRegistries.RECIPES.register(FacadeSwapRecipe.INSTANCE);
        }

        if (BCSiliconItems.plugLens != null) {
            for (EnumDyeColor colour : ColourUtil.COLOURS) {
                String name = String.format("lens-regular-%s", colour.getUnlocalizedName());
                IngredientStack stainedGlass = IngredientStack.of("blockGlass" + ColourUtil.getName(colour));
                ImmutableSet<IngredientStack> input = ImmutableSet.of(stainedGlass);
                ItemStack output = BCSiliconItems.plugLens.getStack(colour, false);
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));

                name = String.format("lens-filter-%s", colour.getUnlocalizedName());
                output = BCSiliconItems.plugLens.getStack(colour, true);
                input = ImmutableSet.of(stainedGlass, IngredientStack.of(new ItemStack(Blocks.IRON_BARS)));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));
            }

            IngredientStack glass = IngredientStack.of("blockGlass");
            ImmutableSet<IngredientStack> input = ImmutableSet.of(glass);
            ItemStack output = BCSiliconItems.plugLens.getStack(null, false);
            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-regular", 500 * MjAPI.MJ, input, output));

            output = BCSiliconItems.plugLens.getStack(null, true);
            input = ImmutableSet.of(glass, IngredientStack.of(new ItemStack(Blocks.IRON_BARS)));
            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-filter", 500 * MjAPI.MJ, input, output));
        }
    }

    private static void makeGateModifierAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
        IngredientStack... mods) {
        for (EnumGateLogic logic : EnumGateLogic.VALUES) {
            String name = String.format("gate-modifier-%s-%s-%s", logic, material, modifier);
            GateVariant variantFrom = new GateVariant(logic, material, EnumGateModifier.NO_MODIFIER);
            ItemStack toUpgrade = BCSiliconItems.plugGate.getStack(variantFrom);
            ItemStack output = BCSiliconItems.plugGate.getStack(new GateVariant(logic, material, modifier));
            Builder<IngredientStack> inputBuilder = new ImmutableSet.Builder<>();
            inputBuilder.add(new IngredientStack(new IngredientNBTBC(toUpgrade)));
            inputBuilder.add(mods);
            ImmutableSet<IngredientStack> input = inputBuilder.build();
            AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));
        }
    }

    private static void makeGateAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier,
        EnumRedstoneChipset chipset, IngredientStack... additional) {
        ImmutableSet.Builder<IngredientStack> temp = ImmutableSet.builder();
        temp.add(new IngredientStack(new IngredientNBTBC(chipset.getStack())));
        temp.add(additional);
        ImmutableSet<IngredientStack> input = temp.build();

        String name = String.format("gate-and-%s-%s", material, modifier);
        ItemStack output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.AND, material, modifier));
        AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));

        name = String.format("gate-or-%s-%s", material, modifier);
        output = BCSiliconItems.plugGate.getStack(new GateVariant(EnumGateLogic.OR, material, modifier));
        AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));
    }

    private static void makeGateRecipe(RecipeBuilderShaped builder, EnumGateMaterial material,
        EnumGateModifier modifier) {
        GateVariant variant = new GateVariant(EnumGateLogic.AND, material, modifier);
        builder.setResult(BCSiliconItems.plugGate.getStack(variant));
        builder.registerNbtAware("buildcraftsilicon:plug_gate_create_" + material + "_" + modifier);
    }

    private static void scanForJsonRecipes() {
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            JsonContext ctx = new JsonContext(mod.getModId());
            CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/assembly_recipes", null, (root, file) -> {
                String path = root.relativize(file).toString();
                if (!FilenameUtils.getExtension(file.toString()).equals("json")) return true;
                String name = FilenameUtils.removeExtension(path).replaceAll("\\\\", "/");
                ResourceLocation key = new ResourceLocation(mod.getModId(), name);
                BufferedReader reader = null;
                try {
                    reader = Files.newBufferedReader(file);
                    JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);
                    if (json == null || json.isJsonNull()) throw new JsonSyntaxException("Json is null (empty file?)");

                    ItemStack output = CraftingHelper.getItemStack(json.getAsJsonObject("result"), ctx);
                    long powercost = json.get("MJ").getAsLong() * MjAPI.MJ;

                    ArrayList<IngredientStack> ingredients = new ArrayList<>();

                    json.getAsJsonArray("components").forEach(element -> {
                        JsonObject object = element.getAsJsonObject();
                        ingredients.add(new IngredientStack(CraftingHelper.getIngredient(object.get("ingredient"), ctx),
                            JsonUtils.getInt(object, "amount", 1)));
                    });

                    AssemblyRecipeRegistry.REGISTRY.put(key,
                        new AssemblyRecipeBasic(key, powercost, ImmutableSet.copyOf(ingredients), output));

                } catch (IOException e) {
                    BCLog.logger.error("Couldn't read recipe {} from {}", key, file, e);
                    return false;
                } finally {
                    IOUtils.closeQuietly(reader);
                }
                return true;
            }, false, false);

            CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/integration_recipes", null, (root, file) -> {
                String path = root.relativize(file).toString();
                if (!FilenameUtils.getExtension(file.toString()).equals("json")) return true;
                String name = FilenameUtils.removeExtension(path).replaceAll("\\\\", "/");
                ResourceLocation key = new ResourceLocation(mod.getModId(), name);
                BufferedReader reader = null;
                try {
                    reader = Files.newBufferedReader(file);
                    JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);
                    if (json == null || json.isJsonNull()) throw new JsonSyntaxException("Json is null (empty file?)");

                    ItemStack output = CraftingHelper.getItemStack(json.getAsJsonObject("result"), ctx);
                    IngredientStack centerStack =
                        IngredientStack.of(CraftingHelper.getIngredient(json.getAsJsonObject("centerStack"), ctx));
                    long powercost = json.get("MJ").getAsLong() * MjAPI.MJ;

                    ArrayList<IngredientStack> ingredients = new ArrayList<>();

                    json.getAsJsonArray("components").forEach(element -> {
                        JsonObject object = element.getAsJsonObject();
                        ingredients.add(new IngredientStack(CraftingHelper.getIngredient(object.get("ingredient"), ctx),
                            JsonUtils.getInt(object, "amount", 1)));
                    });

                    IntegrationRecipeRegistry.INSTANCE
                        .addRecipe(new IntegrationRecipeBasic(key, powercost, centerStack, ingredients, output));

                } catch (IOException e) {
                    BCLog.logger.error("Couldn't read recipe {} from {}", key, file, e);
                    return false;
                } finally {
                    IOUtils.closeQuietly(reader);
                }
                return true;
            }, false, false);
        }
    }
}
