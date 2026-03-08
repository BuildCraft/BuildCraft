package buildcraft.datagen.silicon;

import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.silicon.BCSilicon;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.transport.BCTransportItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class SiliconCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCSilicon.MODID;

    public SiliconCraftingRecipeGenerator(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        // advanced_crafting_table
        ShapedRecipeBuilder.shaped(BCSiliconBlocks.advancedCraftingTable.get())
                .pattern("OtO")
                .pattern("OcO")
                .pattern("OrO")
                .define('r', BCSiliconItems.chipsetRedstone.get())
                .define('c', Tags.Items.CHESTS_WOODEN)
                .define('t', OreDictionaryTags.WORKBENCHES_ITEM)
                .define('O', Tags.Items.OBSIDIAN)
                .unlockedBy("has_item", has(BCSiliconItems.chipsetRedstone.get()))
                .group(MOD_ID)
                .save(consumer);
        // assembly_table
        ShapedRecipeBuilder.shaped(BCSiliconBlocks.assemblyTable.get())
                .pattern("OdO")
                .pattern("OrO")
                .pattern("OgO")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('g', OreDictionaryTags.GEAR_DIAMOND)
                .define('O', Tags.Items.OBSIDIAN)
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_DIAMOND))
                .group(MOD_ID)
                .save(consumer);
        // integration_table
        ShapedRecipeBuilder.shaped(BCSiliconBlocks.integrationTable.get())
                .pattern("OiO")
                .pattern("OrO")
                .pattern("OgO")
                .define('r', BCSiliconItems.chipsetIron.get())
                .define('g', OreDictionaryTags.GEAR_DIAMOND)
                .define('i', Tags.Items.INGOTS_GOLD)
                .define('O', Tags.Items.OBSIDIAN)
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_DIAMOND))
                .group(MOD_ID)
                .save(consumer);
        // programming_table
        ShapedRecipeBuilder.shaped(BCSiliconBlocks.programmingTable.get())
                .pattern("OCO")
                .pattern("ORO")
                .pattern("OGO")
                .define('O', Tags.Items.OBSIDIAN)
                .define('R', BCSiliconItems.chipsetRedstone.get())
                .define('C', Tags.Items.GEMS_EMERALD)
                .define('G', OreDictionaryTags.GEAR_DIAMOND)
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_DIAMOND))
                .group(MOD_ID)
                .save(consumer);
        // charging_table
        ShapedRecipeBuilder.shaped(BCSiliconBlocks.chargingTable.get())
                .pattern("OIO")
                .pattern("OCO")
                .pattern("OGO")
                .define('O', Tags.Items.OBSIDIAN)
                .define('I', Tags.Items.DUSTS_REDSTONE)
                .define('C', BCSiliconItems.chipsetRedstone.get())
                .define('G', OreDictionaryTags.GEAR_GOLD)
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_GOLD))
                .group(MOD_ID)
                .save(consumer);
        // laser
        ShapedRecipeBuilder.shaped(BCSiliconBlocks.laser.get())
                .pattern("rro")
                .pattern("rdd")
                .pattern("rro")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('o', Tags.Items.OBSIDIAN)
                .unlockedBy("has_item", has(Tags.Items.DUSTS_REDSTONE))
                .group(MOD_ID)
                .save(consumer);

        // from BCSiliconRecipes
        // You can craft some of the basic gate types in a normal crafting table

        // Base craftable types
        makeGateRecipe1(Tags.Items.INGOTS_BRICK, EnumGateLogic.AND, EnumGateMaterial.CLAY_BRICK, EnumGateModifier.NO_MODIFIER, consumer);
        makeGateRecipe1(Tags.Items.INGOTS_IRON, EnumGateLogic.AND, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER, consumer);
        makeGateRecipe1(Tags.Items.INGOTS_NETHER_BRICK, EnumGateLogic.AND, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER, consumer);

        // Iron modifier addition
        makeGateRecipe2(Tags.Items.GEMS_LAPIS, EnumGateLogic.AND, EnumGateMaterial.IRON, EnumGateModifier.LAPIS, consumer);
        makeGateRecipe2(Tags.Items.GEMS_QUARTZ, EnumGateLogic.AND, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ, consumer);

        // And Gate <-> Or Gate (shapeless)
        for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
            if (material == EnumGateMaterial.CLAY_BRICK) {
                continue;
            }
            for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                GateVariant varAnd = new GateVariant(EnumGateLogic.AND, material, modifier);
                ItemPluggableGate resultAnd = BCSiliconItems.variantGateMap.get(varAnd).get();

                GateVariant varOr = new GateVariant(EnumGateLogic.OR, material, modifier);
                ItemPluggableGate resultOr = BCSiliconItems.variantGateMap.get(varOr).get();

                ShapelessRecipeBuilder.shapeless(resultAnd)
                        .requires(resultOr)
                        .unlockedBy("has_item", has(Tags.Items.DUSTS_REDSTONE))
                        .group(MOD_ID)
                        .save(consumer);
                ShapelessRecipeBuilder.shapeless(resultOr)
                        .requires(resultAnd)
                        .unlockedBy("has_item", has(Tags.Items.DUSTS_REDSTONE))
                        .group(MOD_ID)
                        .save(consumer);
            }
        }
    }

    private static void makeGateRecipe1(TagKey<Item> m, EnumGateLogic logic, EnumGateMaterial material, EnumGateModifier modifier, Consumer<FinishedRecipe> consumer) {
        GateVariant variant = new GateVariant(logic, material, modifier);
        ShapedRecipeBuilder.shaped(BCSiliconItems.variantGateMap.get(variant).get())
                .pattern(" m ")
                .pattern("mrm")
                .pattern(" b ")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('b', Tags.Items.COBBLESTONE)
                .define('m', m)
                .unlockedBy("has_item", has(Tags.Items.DUSTS_REDSTONE))
                .group(MOD_ID)
                .save(consumer, "buildcraftsilicon:plug_gate_create_" + material.tag + "_" + modifier.tag + "_cobblestone");
        ShapedRecipeBuilder.shaped(BCSiliconItems.variantGateMap.get(variant).get())
                .pattern(" m ")
                .pattern("mrm")
                .pattern(" b ")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('b', BCTransportItems.plugBlocker.get())
                .define('m', m)
                .unlockedBy("has_item", has(Tags.Items.DUSTS_REDSTONE))
                .group(MOD_ID)
                .save(consumer, "buildcraftsilicon:plug_gate_create_" + material.tag + "_" + modifier.tag + "_blocker");
    }

    private static void makeGateRecipe2(TagKey<Item> m, EnumGateLogic logic, EnumGateMaterial material, EnumGateModifier modifier, Consumer<FinishedRecipe> consumer) {
        GateVariant variantG = new GateVariant(EnumGateLogic.AND, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER);
        ItemPluggableGate ironGateG = BCSiliconItems.variantGateMap.get(variantG).get();
        GateVariant variant = new GateVariant(logic, material, modifier);
        ShapedRecipeBuilder.shaped(BCSiliconItems.variantGateMap.get(variant).get())
                .pattern(" m ")
                .pattern("mgm")
                .pattern(" m ")
                .define('g', ironGateG)
                .define('m', m)
                .unlockedBy("has_item", has(Tags.Items.DUSTS_REDSTONE))
                .group(MOD_ID)
                .save(consumer, "buildcraftsilicon:plug_gate_create_" + material.tag + "_" + modifier.tag);
    }

    @Override
    public String getName() {
        return "BuildCraft Silicon Crafting Recipe Generator";
    }
}
