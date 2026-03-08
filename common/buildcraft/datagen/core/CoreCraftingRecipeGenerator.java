package buildcraft.datagen.core;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.core.BCCore;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreItems;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.block.Blocks;
import net.minecraft.data.*;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class CoreCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCCore.MODID;

    public CoreCraftingRecipeGenerator(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        // Calen: unlock the recipe before got the item... some gamerules...
        // Gears
        ShapedRecipeBuilder.shaped(BCCoreItems.gearWood.get())
                .pattern(" o ")
                .pattern("o o")
                .pattern(" o ")
                .define('o', Ingredient.of(Tags.Items.RODS_WOODEN))
                .unlockedBy("has_item", has(Tags.Items.RODS_WOODEN))
                .group(MOD_ID)
                .save(consumer);
        ShapedRecipeBuilder.shaped(BCCoreItems.gearStone.get())
                .pattern(" o ")
                .pattern("oio")
                .pattern(" o ")
                .define('i', Ingredient.of(OreDictionaryTags.GEAR_WOOD))
                .define('o', Ingredient.of(Tags.Items.COBBLESTONE))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_WOOD))
                .group(MOD_ID)
                .save(consumer);
        ShapedRecipeBuilder.shaped(BCCoreItems.gearIron.get())
                .pattern(" o ")
                .pattern("oio")
                .pattern(" o ")
                .define('i', Ingredient.of(OreDictionaryTags.GEAR_STONE))
                .define('o', Ingredient.of(Tags.Items.INGOTS_IRON))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_STONE))
                .group(MOD_ID)
                .save(consumer);
        ShapedRecipeBuilder.shaped(BCCoreItems.gearGold.get())
                .pattern(" o ")
                .pattern("oio")
                .pattern(" o ")
                .define('i', Ingredient.of(OreDictionaryTags.GEAR_IRON))
                .define('o', Ingredient.of(Tags.Items.INGOTS_GOLD))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_IRON))
                .group(MOD_ID)
                .save(consumer);
        ShapedRecipeBuilder.shaped(BCCoreItems.gearDiamond.get())
                .pattern(" o ")
                .pattern("oio")
                .pattern(" o ")
                .define('i', Ingredient.of(OreDictionaryTags.GEAR_GOLD))
                .define('o', Ingredient.of(Tags.Items.GEMS_DIAMOND))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_GOLD))
                .group(MOD_ID)
                .save(consumer);

        // markers
        ShapedRecipeBuilder.shaped(BCCoreBlocks.markerVolume.get())
                .pattern("l")
                .pattern("t")
                .define('l', Ingredient.of(Tags.Items.GEMS_LAPIS))
                .define('t', Ingredient.of(Items.REDSTONE_TORCH))
                .unlockedBy("has_item", has(Items.REDSTONE_TORCH))
                .group(MOD_ID)
                .save(consumer);
        ShapedRecipeBuilder.shaped(BCCoreBlocks.markerPath.get())
                .pattern("g")
                .pattern("t")
                .define('g', Ingredient.of(Tags.Items.DYES_GREEN))
                .define('t', Ingredient.of(Items.REDSTONE_TORCH))
                .unlockedBy("has_item", has(Items.REDSTONE_TORCH))
                .group(MOD_ID)
                .save(consumer);

        // list
        ShapedRecipeBuilder.shaped(BCCoreItems.list.get())
                .pattern("pRp")
                .pattern("pGp")
                .pattern("ppp")
                .define('p', Ingredient.of(Items.PAPER))
                .define('R', Ingredient.of(Tags.Items.DUSTS_REDSTONE))
                .define('G', Ingredient.of(Tags.Items.DYES_GREEN))
                .unlockedBy("has_item", new ImpossibleTrigger.Instance())
                .group(MOD_ID)
                .save(consumer);
        // marker_connector
        ShapedRecipeBuilder.shaped(BCCoreItems.markerConnector.get())
                .pattern("r")
                .pattern("g")
                .pattern("w")
                .define('r', Ingredient.of(Items.REDSTONE_TORCH))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_WOOD))
                .define('w', Ingredient.of(OreDictionaryTags.WRENCH))
                .unlockedBy("has_item", has(OreDictionaryTags.WRENCH))
                .group(MOD_ID)
                .save(consumer);
        // engine
        ShapedRecipeBuilder.shaped(BCCoreBlocks.engineWood.get())
                .pattern("www")
                .pattern(" g ")
                .pattern("GpG")
                .define('w', Ingredient.of(ItemTags.PLANKS))
                .define('g', Ingredient.of(Tags.Items.GLASS_COLORLESS))
                .define('G', Ingredient.of(OreDictionaryTags.GEAR_WOOD))
                .define('p', Ingredient.of(Blocks.PISTON))
                .unlockedBy("has_item", has(Items.PISTON))
                .group(MOD_ID)
                .save(consumer);
        // wrench
        ShapedRecipeBuilder.shaped(BCCoreItems.wrench.get())
                .pattern("I I")
                .pattern(" G ")
                .pattern(" I ")
                .define('I', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('G', Ingredient.of(OreDictionaryTags.GEAR_STONE))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_STONE))
                .group(MOD_ID)
                .save(consumer);

        // paintbrushes
        ShapedRecipeBuilder.shaped(BCCoreItems.paintbrushClean.get())
                .pattern(" iw")
                .pattern(" gi")
                .pattern("s  ")
                .define('i', Ingredient.of(Tags.Items.STRING))
                .define('s', Ingredient.of(Tags.Items.RODS_WOODEN))
                .define('g', Ingredient.of(OreDictionaryTags.GEAR_WOOD))
                .define('w', Ingredient.of(ItemTags.WOOL))
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_WOOD))
                .group(MOD_ID)
                .save(consumer);
        for (DyeColor colour : DyeColor.values()) {
            ShapelessRecipeBuilder.shapeless(BCCoreItems.colourBrushMap.get(colour).get())
                    .requires(OreDictionaryTags.PAINT_BRUSH)
                    .requires(colour.getTag())
                    .unlockedBy("has_item", has(OreDictionaryTags.GEAR_WOOD))
                    .group(MOD_ID)
                    .save(consumer);
        }

        // decorated
        ShapedRecipeBuilder.shaped(BCCoreBlocks.decoratedMap.get(EnumDecoratedBlock.LASER_BACK).get())
                .pattern("sss")
                .pattern("scs")
                .pattern("sss")
                .define('s', Ingredient.of(Tags.Items.OBSIDIAN))
                .define('c', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
                .unlockedBy("has_item", has(Tags.Items.OBSIDIAN))
                .group(MOD_ID)
                .save(consumer);
    }

    @Override
    public String getName() {
        return "BuildCraft Core Crafting IRecipe Generator";
    }
}
