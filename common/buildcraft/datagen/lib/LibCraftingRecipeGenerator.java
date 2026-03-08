package buildcraft.datagen.lib;

import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class LibCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCLib.MODID;

    public LibCraftingRecipeGenerator(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        // guide
        ShapelessRecipeBuilder.shapeless(BCLibItems.guide.get())
                .requires(OreDictionaryTags.GEAR_WOOD)
                .requires(Items.PAPER)
                .requires(Items.PAPER)
                .requires(Items.PAPER)
                .unlockedBy("has_item", has(OreDictionaryTags.WORKBENCHES_ITEM))
                .group(MOD_ID)
                .save(consumer);
    }

    @Override
    public String getName() {
        return "BuildCraft Lib Crafting Recipe Generator";
    }
}
