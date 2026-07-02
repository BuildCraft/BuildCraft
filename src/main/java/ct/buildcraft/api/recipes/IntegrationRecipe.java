package ct.buildcraft.api.recipes;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class IntegrationRecipe {
    public final ResourceLocation name;

    public IntegrationRecipe(ResourceLocation name) {
        this.name = name;
    }

    /**
     * Determines the output of this recipe
     * @param target the stack in the middle to integrate the components into
     * @param toIntegrate All available stacks to integrate (not all have to be used up in this recipe)
     * @return The output to produce based on the inputs provided or an empty stack if the recipe isn't valid
     */
    public abstract ItemStack getOutput(@Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate);

    /**
     * Determines the components to use when crafting finishes
     * @param output The generated outputted, determined by getOutput
     * @return The components to use up
     */
    public abstract ImmutableList<IngredientStack> getRequirements(@Nonnull ItemStack output);

    /**
     * Determines the amount of MJ required to integrate
     * @param output The output that would be generated
     * @return The powercost in microjoules
     */
    public abstract long getRequiredMicroJoules(ItemStack output);

    public abstract IngredientStack getCenterStack();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntegrationRecipe that = (IntegrationRecipe) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
