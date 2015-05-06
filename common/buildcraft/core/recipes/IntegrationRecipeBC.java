package buildcraft.core.recipes;

import buildcraft.api.recipes.IIntegrationRecipe;
import net.minecraft.item.ItemStack;

import java.lang.ref.SoftReference;
import java.util.List;

public abstract class IntegrationRecipeBC implements IIntegrationRecipe {
    private final int energyCost, maxExpansionCount;
    private SoftReference<List<ItemStack>> exampleInputs;
    private SoftReference<List<List<ItemStack>>> exampleExpansions;

    public IntegrationRecipeBC(int energyCost) {
        this(energyCost, -1);
    }

    public IntegrationRecipeBC(int energyCost, int maxExpansionCount) {
        this.energyCost = energyCost;
        this.maxExpansionCount = maxExpansionCount;
    }

    public abstract List<ItemStack> generateExampleInput();
    public abstract List<List<ItemStack>> generateExampleExpansions();

    @Override
    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public List<ItemStack> getExampleInput() {
        if (exampleInputs != null && exampleInputs.get() != null) {
            return exampleInputs.get();
        }
        exampleInputs = new SoftReference<List<ItemStack>>(generateExampleInput());
        return exampleInputs.get();
    }

    @Override
    public List<List<ItemStack>> getExampleExpansions() {
        if (exampleExpansions != null && exampleExpansions.get() != null) {
            return exampleExpansions.get();
        }
        exampleExpansions = new SoftReference<List<List<ItemStack>>>(generateExampleExpansions());
        return exampleExpansions.get();
    }

    @Override
    public int getMaximumExpansionCount() {
        return maxExpansionCount;
    }
}
