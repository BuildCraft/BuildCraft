package buildcraft.datagen.factory;

import buildcraft.energy.BCEnergyFluids;
import buildcraft.factory.BCFactory;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryItems;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class FactoryCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCFactory.MODID;

    public FactoryCraftingRecipeGenerator(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        // autoworkbench_item
        ShapedRecipeBuilder.shaped(BCFactoryBlocks.autoWorkbenchItems.get())
                .pattern("gwg")
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_STONE))
                .define('w', Ingredient.of(OreDictionaryTags.WORKBENCHES_ITEM))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_STONE))
                .group(MOD_ID)
                .save(consumer);
        // chute
        ShapedRecipeBuilder.shaped(BCFactoryBlocks.chute.get())
                .pattern("ici")
                .pattern("igi")
                .pattern(" i ")
                .define('c', Ingredient.of(Tags.Items.CHESTS_WOODEN))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_STONE))
                .define('i', Ingredient.of(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_STONE))
                .group(MOD_ID)
                .save(consumer);
        // distiller
        ShapedRecipeBuilder.shaped(BCFactoryBlocks.distiller.get())
                .pattern("rtr")
                .pattern("tgt")
                .define('r', Ingredient.of(Items.REDSTONE_TORCH))
                .define('t', Ingredient.of(BCFactoryBlocks.tank.get()))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_DIAMOND))
                .unlockedBy("has_item", has(BCFactoryBlocks.tank.get()))
                .group(MOD_ID)
                .save(consumer);
        // flood_gate
        ShapedRecipeBuilder.shaped(BCFactoryBlocks.floodGate.get())
                .pattern("igi")
                .pattern("btb")
                .pattern("ibi")
                .define('b', Ingredient.of(Items.IRON_BARS))
                .define('t', Ingredient.of(BCFactoryBlocks.tank.get()))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_IRON))
                .define('i', Ingredient.of(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_item", has(BCFactoryBlocks.tank.get()))
                .group(MOD_ID)
                .save(consumer);
        // heat_exchange
        ShapedRecipeBuilder.shaped(BCFactoryBlocks.heatExchange.get())
                .pattern("igi")
                .pattern("###")
                .pattern("igi")
                .define('i', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_IRON))
                .define('#', Ingredient.of(Tags.Items.GLASS_COLORLESS))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_IRON))
                .group(MOD_ID)
                .save(consumer);
        // mining_well
        ShapedRecipeBuilder.shaped(BCFactoryBlocks.miningWell.get())
                .pattern("iri")
                .pattern("igi")
                .pattern("ipi")
                .define('p', Ingredient.of(Items.IRON_PICKAXE))
                .define('r', Ingredient.of(Tags.Items.DUSTS_REDSTONE))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_IRON))
                .define('i', Ingredient.of(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_IRON))
                .group(MOD_ID)
                .save(consumer);
        // pump
        ShapedRecipeBuilder.shaped(BCFactoryBlocks.pump.get())
                .pattern("iri")
                .pattern("igi")
                .pattern("tbt")
                .define('r', Ingredient.of(Tags.Items.DUSTS_REDSTONE))
                .define('b', Ingredient.of(Items.BUCKET))
                .define('t', Ingredient.of(BCFactoryBlocks.tank.get()))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_IRON))
                .define('i', Ingredient.of(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_item", has(BCFactoryBlocks.tank.get()))
                .group(MOD_ID)
                .save(consumer);
        // tank
        ShapedRecipeBuilder.shaped(BCFactoryBlocks.tank.get())
                .pattern("ggg")
                .pattern("g g")
                .pattern("ggg")
                .define('g', Ingredient.of(Tags.Items.GLASS_COLORLESS))
                .unlockedBy("has_item", has(Tags.Items.GLASS))
                .group(MOD_ID)
                .save(consumer);
        // water_gel_spawn
        ShapedRecipeBuilder.shaped(BCFactoryItems.waterGel.get())
                .pattern(" s ")
                .pattern("srs")
                .pattern(" s ")
                .define('s', Ingredient.of(Tags.Items.SAND))
                .define('r', Ingredient.of(BCEnergyFluids.oilResidue[0].get().getBucket()))
                .unlockedBy("has_item", has(Tags.Items.SAND))
                .group(MOD_ID)
                .save(consumer);
        // water_bucket
        ShapedRecipeBuilder.shaped(Items.WATER_BUCKET)
                .pattern("g")
                .pattern("b")
                .define('g', Ingredient.of(BCFactoryItems.gelledWater.get()))
                .define('b', Ingredient.of(Items.BUCKET))
                .unlockedBy("has_item", has(Items.BUCKET))
                .group(MOD_ID)
                .save(consumer, MOD_ID + ":water_gel_to_bucket");
    }

    @Override
    public String getName() {
        return "BuildCraft Factory Crafting IRecipe Generator";
    }
}
