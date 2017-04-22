package buildcraft.lib.recipe;

import java.util.*;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.recipes.IRefineryRecipeManager;

public enum RefineryRecipeRegistry implements IRefineryRecipeManager {
    INSTANCE;

    public final IRefineryRegistry<IDistillationRecipe> distillationRegistry = new SingleRegistry<>();
    public final IRefineryRegistry<IHeatableRecipe> heatableRegistry = new SingleRegistry<>();
    public final IRefineryRegistry<ICoolableRecipe> coolableRegistry = new SingleRegistry<>();

    @Override
    public IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks) {
        return new HeatableRecipe(ticks, in, out, heatFrom, heatTo);
    }

    @Override
    public ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks) {
        return new CoolableRecipe(ticks, in, out, heatFrom, heatTo);
    }

    @Override
    public IDistillationRecipe createDistillationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, long powerRequired) {
        return new DistillationRecipe(powerRequired, in, outGas, outLiquid);
    }

    @Override
    public IRefineryRegistry<IHeatableRecipe> getHeatableRegistry() {
        return heatableRegistry;
    }

    @Override
    public IRefineryRegistry<ICoolableRecipe> getCoolableRegistry() {
        return coolableRegistry;
    }

    @Override
    public IRefineryRegistry<IDistillationRecipe> getDistilationRegistry() {
        return distillationRegistry;
    }

    private static class SingleRegistry<R extends IRefineryRecipe> implements IRefineryRegistry<R> {
        private final List<R> allRecipes = new LinkedList<>();

        @Override
        public Stream<R> getRecipes(Predicate<R> filter) {
            return allRecipes.stream().filter(r -> filter.apply(r));
        }

        @Override
        public Collection<R> getAllRecipes() {
            return allRecipes;
        }

        @Override
        @Nullable
        public R getRecipeForInput(@Nullable FluidStack fluid) {
            if (fluid == null) {
                return null;
            }
            for (R recipe : allRecipes) {
                if (recipe.in().isFluidEqual(fluid)) {
                    return recipe;
                }
            }
            return null;
        }

        @Override
        public Collection<R> removeRecipes(Predicate<R> toRemove) {
            List<R> removed = new ArrayList<>();
            Iterator<R> iter = allRecipes.iterator();
            while (iter.hasNext()) {
                R recipe = iter.next();
                if (toRemove.apply(recipe)) {
                    iter.remove();
                    removed.add(recipe);
                }
            }
            return removed;
        }

        @Override
        public R addRecipe(R recipe) {
            if (recipe == null) throw new NullPointerException("recipe");
            ListIterator<R> iter = allRecipes.listIterator();
            while (iter.hasNext()) {
                R existing = iter.next();
                if (existing.in().isFluidEqual(recipe.in())) {
                    iter.set(recipe);
                    return recipe;
                }
            }
            allRecipes.add(recipe);
            return recipe;
        }
    }

    public static abstract class RefineryRecipe implements IRefineryRecipe {
        private final FluidStack in;

        public RefineryRecipe(FluidStack in) {
            this.in = in;
        }

        @Override
        public FluidStack in() {
            return in;
        }
    }

    public static class DistillationRecipe extends RefineryRecipe implements IDistillationRecipe {
        private final FluidStack outGas, outLiquid;
        private final long powerRequired;

        public DistillationRecipe(long powerRequired, FluidStack in, FluidStack outGas, FluidStack outLiquid) {
            super(in);
            this.powerRequired = powerRequired;
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

        @Override
        public long powerRequired() {
            return powerRequired;
        }
    }

    public static abstract class HeatExchangeRecipe extends RefineryRecipe implements IHeatExchangerRecipe {
        private final int ticks;
        private final FluidStack out;
        private final int heatFrom, heatTo;

        public HeatExchangeRecipe(int ticks, FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(in);
            this.ticks = ticks;
            this.out = out;
            this.heatFrom = heatFrom;
            this.heatTo = heatTo;
        }

        @Override
        public int ticks() {
            return ticks;
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

    public static class HeatableRecipe extends HeatExchangeRecipe implements IHeatableRecipe {
        public HeatableRecipe(int ticks, FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(ticks, in, out, heatFrom, heatTo);
        }
    }

    public static class CoolableRecipe extends HeatExchangeRecipe implements ICoolableRecipe {
        public CoolableRecipe(int ticks, FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(ticks, in, out, heatFrom, heatTo);
        }
    }
}
