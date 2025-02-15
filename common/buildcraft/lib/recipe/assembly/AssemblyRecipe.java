package buildcraft.lib.recipe.assembly;

import buildcraft.api.recipes.IAssemblyRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

//public abstract class AssemblyRecipe implements Comparable<AssemblyRecipe>, IForgeRegistryEntry<AssemblyRecipe>
public abstract class AssemblyRecipe implements IAssemblyRecipe {
    protected ResourceLocation name;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AssemblyRecipe that = (AssemblyRecipe) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    // Recipe

    @Override
    public ResourceLocation getId() {
        return name;
    }

    @Override
    public RecipeSerializer<IAssemblyRecipe> getSerializer() {
        return AssemblyRecipeSerializer.INSTANCE;
    }
}
