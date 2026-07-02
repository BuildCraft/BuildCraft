package ct.buildcraft.lib.recipe;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.recipes.IngredientStack;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * @deprecated TEMPORARY CLASS DO NOT USE!
 */
@Deprecated
public abstract class $AssemblyRecipe implements Comparable<$AssemblyRecipe>/*, IForgeRegistryEntry<AssemblyRecipe>*/{
    private ResourceLocation name;

    /**
     * The outputs this recipe can generate with any of the given inputs
     * @param inputs Current ingredients in the assembly table
     * @return A Set containing all possible outputs given the given inputs or an empty one if nothing can be assembled from the given inputs
     */
    public abstract Set<ItemStack> getOutputs(NonNullList<ItemStack> inputs);

    /**
     * Used to determine all outputs from this recipe for recipe previews (guide book and/or JEI)
     */
    public abstract Set<ItemStack> getOutputPreviews();

    /**
     * Used to determine what items to use up for the given output
     * @param output The output we want to know the inputs for, only ever called using stacks obtained from getOutputs or getOutputPreviews
     */
    public abstract Set<IngredientStack> getInputsFor(@Nonnull ItemStack output);

    /**
     * Used to determine how much MJ is required to asemble the given output item
     * @param output The output we want to know the MJ cost for, only ever called using stacks obtained from getOutputs or getOutputPreviews
     */
    public abstract long getRequiredMicroJoulesFor(@Nonnull ItemStack output);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        $AssemblyRecipe that = ($AssemblyRecipe) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo($AssemblyRecipe o) {
        return name.toString().compareTo(o.name.toString());
    }

   // @Override
    public $AssemblyRecipe setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
   // @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    //@Override
    public Class<$AssemblyRecipe> getRegistryType() {
        return $AssemblyRecipe.class;
    }
}
