package buildcraft.datagen.robotics;

import buildcraft.factory.BCFactory;
import buildcraft.lib.oredictionarytag.OreDictionaryTags;
import buildcraft.robotics.BCRoboticsBlocks;
import buildcraft.transport.BCTransportItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public class RoboticsCraftingRecipeGenerator extends RecipeProvider {
    private static final String MOD_ID = BCFactory.MODID;

    public RoboticsCraftingRecipeGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // zonePlanner
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCRoboticsBlocks.zonePlanner.get())
                .pattern("wdw")
                .pattern("wcw")
                .pattern("wpw")
                .define('d', (Item) BCTransportItems.pipeItemDiamond.get(null).get())
                .define('w', ItemTags.PLANKS)
                .define('p', Blocks.PISTON)
                .define('c', Blocks.CHEST)
                .unlockedBy("has_item", has(OreDictionaryTags.GEAR_STONE))
                .group(MOD_ID)
                .save(consumer);
    }

    @Override
    public String getName() {
        return "BuildCraft Robotics Crafting Recipe Generator";
    }
}
