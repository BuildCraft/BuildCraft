package buildcraft.datagen.robotics;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
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
        for (RedstoneBoardNBT<?> boardNBT : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            ItemStack output = RedstoneBoardRegistry.instance.getBoardNBTItemMap().get(boardNBT).get().getDefaultInstance();
            ProgrammingRecipeBuilder.programming(output, RedstoneBoardRegistry.instance.getPowerCost(boardNBT)).save(consumer, boardNBT.getID().getPath());
        }
    }

    @Override
    public String getName() {
        return "BuildCraft Programming Integration Recipe Generator";
    }
}
