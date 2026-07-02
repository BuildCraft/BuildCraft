package ct.buildcraft.lib.recipe;

import java.util.Set;

import javax.annotation.Nonnull;

import ct.buildcraft.api.recipes.IngredientStack;
import ct.buildcraft.silicon.BCSiliconItems;
import ct.buildcraft.silicon.BCSiliconRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class AssemblyRecipeBasic implements Recipe<Container>, Comparable<AssemblyRecipeBasic>{
	   protected ResourceLocation name;

	    /**
	     * The outputs this recipe can generate with any of the given inputs
	     * @param inputs Current ingredients in the assembly table
	     * @return A Set containing all possible outputs given the given inputs or an empty one if nothing can be assembled from the given inputs
	     */
	    public abstract Set<ItemStack> getOutputs(IItemHandlerModifiable inputs);

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
		public boolean matches(Container container, Level p_44003_) {
			return !getOutputs(new InvWrapper(container)).isEmpty();
		}

		@Override
		public ItemStack assemble(Container container) {
			Set<ItemStack> outputs = getOutputs(new InvWrapper(container));
			return outputs.isEmpty() ? ItemStack.EMPTY : outputs.toArray(new ItemStack[1])[0];
		}
		
		@Override
		public ItemStack getToastSymbol() {
			return new ItemStack(BCSiliconItems.ASSEMBLY_TABLE_ITEM.get());
		}

		@Override
	    public boolean equals(Object o) {
	        if (this == o) {
	            return true;
	        }
	        if (o == null || getClass() != o.getClass()) {
	            return false;
	        }

	        AssemblyRecipe that = (AssemblyRecipe) o;

	        return name.equals(that.name);
	    }

	    @Override
	    public int hashCode() {
	        return name.hashCode();
	    }

	    @Override
	    public int compareTo(AssemblyRecipeBasic o) {
	        return name.toString().compareTo(o.name.toString());
	    }
	    
	    @Override
		public ResourceLocation getId() {
			return name;
		}

		@Override
		public RecipeType<?> getType() {
			return BCSiliconRecipes.ASSEMBLY_TYPE.get();
		}
		
		
}
