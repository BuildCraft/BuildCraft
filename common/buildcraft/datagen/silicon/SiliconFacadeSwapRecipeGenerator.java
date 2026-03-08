package buildcraft.datagen.silicon;

import buildcraft.silicon.recipe.FacadeSwapRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;

import java.util.function.Consumer;

public class SiliconFacadeSwapRecipeGenerator extends RecipeProvider {
    public SiliconFacadeSwapRecipeGenerator(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        // Facade swap
        FacadeSwapRecipeBuilder.swap("facade_swap").save(consumer);
    }

    @Override
    public String getName() {
        return "BuildCraft Silicon Facade Swap IRecipe Generator";
    }
}
