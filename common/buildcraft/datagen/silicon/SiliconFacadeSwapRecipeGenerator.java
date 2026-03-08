package buildcraft.datagen.silicon;

import buildcraft.silicon.recipe.FacadeSwapRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

public class SiliconFacadeSwapRecipeGenerator extends RecipeProvider {
    public SiliconFacadeSwapRecipeGenerator(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        // Facade swap
        FacadeSwapRecipeBuilder.swap("facade_swap").save(consumer);
    }

    @Override
    public String getName() {
        return "BuildCraft Silicon Facade Swap Recipe Generator";
    }
}
