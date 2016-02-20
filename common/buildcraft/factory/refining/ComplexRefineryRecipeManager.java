package buildcraft.factory.refining;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.recipes.IComplexRefineryRecipeManager;

public enum ComplexRefineryRecipeManager implements IComplexRefineryRecipeManager {
    INSTANCE;

    private final ComplexRefineryRegistry<IHeatableRecipe> heatableRegistry = new ComplexRefineryRegistry<>();
    private final ComplexRefineryRegistry<ICoolableRecipe> coolantRegistry = new ComplexRefineryRegistry<>();
    private final ComplexRefineryRegistry<IDistilationRecipe> distillationRegistry = new ComplexRefineryRegistry<>();

    @Override
    public IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks) {
        return new HeatableRecipe(ticks, in, out, heatFrom, heatTo);
    }

    @Override
    public ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks) {
        return new CoolableRecipe(ticks, in, out, heatFrom, heatTo);
    }

    @Override
    public IDistilationRecipe createDistilationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, int ticks) {
        return new DistilationRecipe(in, ticks, outGas, outLiquid);
    }

    @Override
    public IComplexRefineryRegistry<IHeatableRecipe> getHeatableRegistry() {
        return heatableRegistry;
    }

    @Override
    public IComplexRefineryRegistry<ICoolableRecipe> getCoolableRegistry() {
        return coolantRegistry;
    }

    @Override
    public IComplexRefineryRegistry<IDistilationRecipe> getDistilationRegistry() {
        return distillationRegistry;
    }

    private static class ComplexRefineryRegistry<R extends IComplexRefineryRecipe> implements IComplexRefineryRegistry<R> {
        private final Set<R> recipeSet = new HashSet<>();

        @Override
        public R addRecipe(R recipe, boolean replaceExisting) {
            if (recipe == null) throw new NullPointerException("recipe");
            R existing = recipeSet.stream().filter(r -> r.equals(recipe)).findAny().orElse(null);
            if (existing == null) {
                recipeSet.add(recipe);
                return recipe;
            } else if (replaceExisting) {
                recipeSet.remove(existing);
                recipeSet.add(recipe);
                return recipe;
            } else {
                return existing;
            }
        }

        @Override
        public Stream<R> getRecipes(Predicate<R> toReturn) {
            return recipeSet.stream().filter(toReturn);
        }

        @Override
        public Set<R> removeRecipes(Predicate<R> toRemove) {
            Set<R> removeSet = getRecipesAsSet(toRemove);
            recipeSet.removeAll(removeSet);
            return removeSet;
        }
    }

    private static abstract class ComplexRefineryRecipe implements IComplexRefineryRecipe {
        private final int ticks;
        private final FluidStack in;

        public ComplexRefineryRecipe(FluidStack in, int ticks) {
            this.in = in;
            this.ticks = ticks;
        }

        @Override
        public FluidStack in() {
            return in;
        }

        @Override
        public int ticks() {
            return ticks;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj.getClass() != getClass()) return false;
            return in().equals(((IComplexRefineryRecipe) obj).in());
        }

        @Override
        public int hashCode() {
            return in().hashCode();
        }
    }

    public static abstract class HeatExchangerRecipe extends ComplexRefineryRecipe implements IHeatExchangerRecipe {
        private final FluidStack out;
        private final int heatFrom, heatTo;

        public HeatExchangerRecipe(int ticks, FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(in, ticks);
            this.out = out;
            this.heatFrom = heatFrom;
            this.heatTo = heatTo;
        }

        @Override
        public FluidStack out() {
            return out;
        }

        @Override
        public int heatFrom() {
            return heatFrom;
        }

        @Override
        public int heatTo() {
            return heatTo;
        }
    }

    public static class HeatableRecipe extends HeatExchangerRecipe implements IHeatableRecipe {
        public HeatableRecipe(int ticks, FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(ticks, in, out, heatFrom, heatTo);
            if (heatFrom >= heatTo) throw new IllegalArgumentException("Tried to add a heatable recipe from a higher heat value to a lower one!");
        }
    }

    public static class CoolableRecipe extends HeatExchangerRecipe implements ICoolableRecipe {
        public CoolableRecipe(int ticks, FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(ticks, in, out, heatFrom, heatTo);
            if (heatFrom <= heatTo) throw new IllegalArgumentException("Tried to add a coolant recipe from a lower heat value to a higher one!");
        }
    }

    public static class DistilationRecipe extends ComplexRefineryRecipe implements IDistilationRecipe {
        private final FluidStack outGas, outLiquid;

        public DistilationRecipe(FluidStack in, int ticks, FluidStack outGas, FluidStack outLiquid) {
            super(in, ticks);
            this.outGas = outGas;
            this.outLiquid = outLiquid;
        }

        @Override
        public FluidStack outGas() {
            return outGas;
        }

        @Override
        public FluidStack outLiquid() {
            return outLiquid;
        }
    }
}
