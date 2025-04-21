package buildcraft.datagen.builders;

import buildcraft.builders.BCBuilders;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersItems;
import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class BuildersCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCBuilders.MODID;

    public BuildersCraftingRecipeGenerator(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        // architect
        ShapedRecipeBuilder.shaped(BCBuildersBlocks.architect.get())
                .pattern("bmb")
                .pattern("ycy")
                .pattern("dsd")
                .define('b', Ingredient.of(Tags.Items.DYES_BLACK))
                .define('m', Ingredient.of(BCCoreBlocks.markerVolume.get()))
                .define('y', Ingredient.of(Tags.Items.DYES_YELLOW))
                .define('c', Ingredient.of(Items.CRAFTING_TABLE))
                .define('d', Ingredient.of(OreDictionaryTags.GEAR_DIAMOND))
                .define('s', Ingredient.of(BCBuildersItems.snapshotBLUEPRINT.get()))
                .unlockedBy("has_item", has(BCCoreBlocks.markerVolume.get()))
                .group(MOD_ID)
                .save(consumer);
        // blueprint
        ShapedRecipeBuilder.shaped(BCBuildersItems.snapshotBLUEPRINT.get())
                .pattern("ppp")
                .pattern("pbp")
                .pattern("ppp")
                .define('p', Ingredient.of(Items.PAPER))
                .define('b', Ingredient.of(Tags.Items.GEMS_LAPIS))
                .unlockedBy("has_item", has(Items.PAPER))
                .group(MOD_ID)
                .save(consumer);
        // quarry
        ShapedRecipeBuilder.shaped(BCBuildersBlocks.quarry.get())
                .pattern("iri")
                .pattern("gig")
                .pattern("dpd")
                .define('p', Ingredient.of(Items.DIAMOND_PICKAXE))
                .define('i', Ingredient.of(OreDictionaryTags.GEAR_IRON))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_GOLD))
                .define('d', Ingredient.of(OreDictionaryTags.GEAR_DIAMOND))
                .define('r', Ingredient.of(Tags.Items.DUSTS_REDSTONE))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_DIAMOND))
                .group(MOD_ID)
                .save(consumer);
        // template
        ShapedRecipeBuilder.shaped(BCBuildersItems.snapshotTEMPLATE.get())
                .pattern("ppp")
                .pattern("pbp")
                .pattern("ppp")
                .define('p', Ingredient.of(Items.PAPER))
                .define('b', Ingredient.of(Tags.Items.DYES_BLACK))
                .unlockedBy("has_item", has(Items.PAPER))
                .group(MOD_ID)
                .save(consumer);
        // builder
        ShapedRecipeBuilder.shaped(BCBuildersBlocks.builder.get())
                .pattern("bmb")
                .pattern("ycy")
                .pattern("dsd")
                .define('b', Ingredient.of(Tags.Items.DYES_BLACK))
                .define('m', Ingredient.of(BCCoreBlocks.markerVolume.get()))
                .define('y', Ingredient.of(Tags.Items.DYES_YELLOW))
                .define('c', Ingredient.of(OreDictionaryTags.WORKBENCHES_ITEM))
                .define('d', Ingredient.of(OreDictionaryTags.GEAR_DIAMOND))
                .define('s', Ingredient.of(Tags.Items.CHESTS_WOODEN))
                .unlockedBy("has_item", has(BCCoreBlocks.markerVolume.get()))
                .group(MOD_ID)
                .save(consumer);
        // library
        ShapedRecipeBuilder.shaped(BCBuildersBlocks.library.get())
                .pattern("igi")
                .pattern("sbs")
                .pattern("iri")
                .define('i', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_IRON))
                .define('s', Ingredient.of(Tags.Items.BOOKSHELVES))
                .define('b', Ingredient.of(BCBuildersItems.snapshotBLUEPRINT.get()))
                .define('r', Ingredient.of(Tags.Items.DUSTS_REDSTONE))
                .unlockedBy("has_item", has(Items.BOOKSHELF))
                .group(MOD_ID)
                .save(consumer);
        // markerConstruction
        ShapedRecipeBuilder.shaped(BCBuildersBlocks.markerConstruction.get())
                .pattern("l")
                .pattern("r")
                .define('l', Ingredient.of(OreDictionaryTags.GEAR_GOLD))
                .define('r', Ingredient.of(Items.REDSTONE_TORCH))
                .unlockedBy("has_item", has(Items.REDSTONE_TORCH))
                .group(MOD_ID)
                .save(consumer);
        // schematicSingle
        ShapelessRecipeBuilder.shapeless(BCBuildersItems.schematicSingle.get(), 4)
                .requires(Items.PAPER)
                .requires(Items.PAPER)
                .requires(Tags.Items.GEMS_LAPIS)
                .unlockedBy("has_item", has(Items.PAPER))
                .group(MOD_ID)
                .save(consumer);
    }

    @Override
    public String getName() {
        return "BuildCraft Builders Crafting IRecipe Generator";
    }
}
