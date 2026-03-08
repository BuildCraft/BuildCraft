package buildcraft.datagen.robotics;

import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.recipe.integration.IntegrationRecipeBuilder;
import buildcraft.robotics.BCRoboticsItems;
import com.google.common.collect.ImmutableList;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class RoboticsIntegrationRecipeGenerator extends RecipeProvider {
    private final ExistingFileHelper existingFileHelper;

    private Consumer<IFinishedRecipe> consumer;

    public RoboticsIntegrationRecipeGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        this.consumer = consumer;

        BCRoboticsItems.robot.keySet().forEach(this::robot);
    }

    private void robot(RedstoneBoardRobotNBT boardNBT) {
        if (boardNBT != RedstoneBoardRegistry.instance.getEmptyRobotBoard()) {
            IntegrationRecipeBuilder.basic(
                    5000 * MjAPI.MJ,
                    IngredientStack.of(BCRoboticsItems.robot.get(RedstoneBoardRegistry.instance.getEmptyRobotBoard()).get()),
                    ImmutableList.of(IngredientStack.of(BCRoboticsItems.redstoneBoard.get(boardNBT).get())),
                    BCRoboticsItems.robot.get(boardNBT).get().getDefaultInstance()
            ).save(consumer, boardNBT.getRobotId().getPath());
        }
    }

    @Override
    public String getName() {
        return "BuildCraft Robotics Integration Recipe Generator";
    }
}
