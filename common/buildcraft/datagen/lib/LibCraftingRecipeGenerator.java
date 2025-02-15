package buildcraft.datagen.lib;

import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class LibCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCLib.MODID;

    public LibCraftingRecipeGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // guide
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BCLibItems.guide.get())
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
