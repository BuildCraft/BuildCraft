package buildcraft.datagen.robotics;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.recipe.programming.ProgrammingRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class RoboticsProgrammingRecipeGenerator extends RecipeProvider {
    private final ExistingFileHelper existingFileHelper;

    public RoboticsProgrammingRecipeGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        IngredientStack input = IngredientStack.of(new ItemStack(RedstoneBoardRegistry.instance.getBoardNBTItemMap().get(RedstoneBoardRegistry.instance.getEmptyRobotBoard()).get()));
        for (RedstoneBoardNBT<?> boardNBT : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            ItemStack output = RedstoneBoardRegistry.instance.getBoardNBTItemMap().get(boardNBT).get().getDefaultInstance();
            ProgrammingRecipeBuilder.programming(input, output, RedstoneBoardRegistry.instance.getPowerCost(boardNBT)).save(consumer, boardNBT.getID().getPath());
        }
    }

    @Override
    public String getName() {
        return "BuildCraft Programming Integration Recipe Generator";
    }
}
