package buildcraft.lib.recipe;

import java.util.*;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.recipes.IRefineryRecipeManager;

public enum RefineryRecipeRegistry implements IRefineryRecipeManager {
    INSTANCE;

    public final IRefineryRegistry<IDistilationRecipe> distillationRegistry = new SingleRegistry<>();
    public final IRefineryRegistry<IHeatableRecipe> heatableRegistry = new SingleRegistry<>();
    public final IRefineryRegistry<ICoolableRecipe> coolableRegistry = new SingleRegistry<>();

    @Override
    public IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public IHeatableRecipe addHeatableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks, boolean replaceExisting) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public ICoolableRecipe addCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks, boolean replaceExisting) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public IDistilationRecipe createDistilationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, int ticks) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public IDistilationRecipe addDistilationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, int ticks, boolean replaceExisting) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
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
    public IRefineryRegistry<IDistilationRecipe> getDistilationRegistry() {
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
        public R getRecipeForInput(FluidStack fluid) {
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
        public R addRecipe(R recipe, boolean replaceExisting) {
            if (recipe == null) throw new NullPointerException("recipe");
            ListIterator<R> iter = allRecipes.listIterator();
            while (iter.hasNext()) {
                R existing = iter.next();
                if (existing.in().isFluidEqual(recipe.in())) {
                    if (replaceExisting) {
                        iter.set(recipe);
                        return recipe;
                    } else {
                        return existing;
                    }
                }
            }
            allRecipes.add(recipe);
            return recipe;
        }
    }

    public static abstract class RefineryRecipe implements IRefineryRecipe {
        private final int ticks;
        private final FluidStack in;

        public RefineryRecipe(int ticks, FluidStack in) {
            this.ticks = ticks;
            this.in = in;
        }

        @Override
        public int ticks() {
            return ticks;
        }

        @Override
        public FluidStack in() {
            return in;
        }
    }

    public static class DistillationRecipe extends RefineryRecipe implements IDistilationRecipe {
        private final FluidStack outGas, outLiquid;

        public DistillationRecipe(int ticks, FluidStack in, FluidStack outGas, FluidStack outLiquid) {
            super(ticks, in);
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

    public static abstract class HeatExchangeRecipe extends RefineryRecipe implements IHeatExchangerRecipe {
        private final FluidStack out;
        private final int heatFrom, heatTo;

        public HeatExchangeRecipe(int ticks, FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(ticks, in);
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
