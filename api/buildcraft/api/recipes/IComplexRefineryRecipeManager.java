package buildcraft.api.recipes;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraftforge.fluids.FluidStack;

public interface IComplexRefineryRecipeManager {
    IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks);

    default IHeatableRecipe addHeatableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks, boolean replaceExisting) {
        return getHeatableRegistry().addRecipe(createHeatingRecipe(in, out, heatFrom, heatTo, ticks), replaceExisting);
    }

    ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks);

    default ICoolableRecipe addCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks, boolean replaceExisting) {
        return getCoolableRegistry().addRecipe(createCoolableRecipe(in, out, heatFrom, heatTo, ticks), replaceExisting);
    }

    IDistilationRecipe createDistilationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, int ticks);

    default IDistilationRecipe addDistilationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, int ticks, boolean replaceExisting) {
        return getDistilationRegistry().addRecipe(createDistilationRecipe(in, outGas, outLiquid, ticks), replaceExisting);
    }

    IComplexRefineryRegistry<IHeatableRecipe> getHeatableRegistry();

    IComplexRefineryRegistry<ICoolableRecipe> getCoolableRegistry();

    IComplexRefineryRegistry<IDistilationRecipe> getDistilationRegistry();

    public interface IComplexRefineryRegistry<R extends IComplexRefineryRecipe> {
        /** @return an unmodifiable set containing all of the distilation recipies that satisfy the given predicate. All
         *         of the recipe objects are guarenteed to never be null. */
        Stream<R> getRecipes(Predicate<R> toReturn);

        default Set<R> getRecipesAsSet(Predicate<R> toReturn) {
            return getRecipes(toReturn).collect(Collectors.toCollection(() -> new HashSet<>()));
        }

        /** @return an unmodifiable set containing all of the distilation recipies. */
        default Set<R> getAllRecipes() {
            return getRecipesAsSet(r -> true);
        }

        default R getRecipeForInput(FluidStack fluid) {
            return getRecipes(r -> r.in().equals(fluid)).findAny().orElse(null);
        }

        Set<R> removeRecipes(Predicate<R> toRemove);

        R addRecipe(R recipe, boolean replaceExisting);
    }

    public interface IComplexRefineryRecipe {
        int ticks();

        FluidStack in();
    }

    public interface IHeatExchangerRecipe extends IComplexRefineryRecipe {
        FluidStack out();

        int heatFrom();

        int heatTo();
    }

    public interface IHeatableRecipe extends IHeatExchangerRecipe {}

    public interface ICoolableRecipe extends IHeatExchangerRecipe {}

    public interface IDistilationRecipe extends IComplexRefineryRecipe {
        FluidStack outGas();

        FluidStack outLiquid();
    }
}
