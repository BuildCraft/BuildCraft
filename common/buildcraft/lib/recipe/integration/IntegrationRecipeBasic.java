package buildcraft.lib.recipe.integration;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.lib.misc.StackUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

public class IntegrationRecipeBasic extends IntegrationRecipe {
    protected final long requiredMicroJoules;
    protected final IngredientStack target;
    protected final ImmutableList<IngredientStack> toIntegrate;
    protected final @Nonnull ItemStack output;

    public IntegrationRecipeBasic(ResourceLocation name, long requiredMicroJoules, IngredientStack target, List<IngredientStack> toIntegrate, @Nonnull ItemStack output) {
        super(name);
        this.requiredMicroJoules = requiredMicroJoules;
        this.target = target;
        this.toIntegrate = ImmutableList.copyOf(toIntegrate);
        this.output = output;
    }

    public IntegrationRecipeBasic(String name, long requiredMicroJoules, IngredientStack target, List<IngredientStack> toIntegrate, @Nonnull ItemStack output) {
        this(BuildCraftAPI.nameToResourceLocation(name), requiredMicroJoules, target, toIntegrate, output);
    }


    protected boolean matches(@Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate) {
        if (!StackUtil.contains(this.target, target)) {
            return false;
        }
        NonNullList<ItemStack> toIntegrateCopy = toIntegrate.stream().filter(stack -> !stack.isEmpty()).collect(StackUtil.nonNullListCollector());
        boolean stackMatches = this.toIntegrate.stream().allMatch((definition) ->
        {
            boolean matches = false;
            Iterator<ItemStack> iterator = toIntegrateCopy.iterator();
            while (iterator.hasNext()) {
                ItemStack stack = iterator.next();
                if (StackUtil.contains(definition, stack)) {
                    matches = true;
                    iterator.remove();
                    break;
                }
            }
            return matches;
        });
        return stackMatches && toIntegrateCopy.isEmpty();
    }

    @Override
    public ItemStack getOutput(@Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate) {
        return matches(target, toIntegrate) ? output : ItemStack.EMPTY;
    }

    @Override
    public ImmutableList<IngredientStack> getRequirements(ItemStack output) {
        return toIntegrate;
    }

    @Override
    public long getRequiredMicroJoules(ItemStack output) {
        return requiredMicroJoules;
    }

    @Override
    public IngredientStack getCenterStack() {
        return target;
    }
}
