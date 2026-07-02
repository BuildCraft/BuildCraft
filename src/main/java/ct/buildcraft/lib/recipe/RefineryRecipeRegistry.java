/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import ct.buildcraft.api.recipes.IRefineryRecipeManager;
import net.minecraftforge.fluids.FluidStack;

public enum RefineryRecipeRegistry implements IRefineryRecipeManager {
    INSTANCE;

    public final IRefineryRegistry<IDistillationRecipe> distillationRegistry = new SingleRegistry<>();
    public final IRefineryRegistry<IHeatableRecipe> heatableRegistry = new SingleRegistry<>();
    public final IRefineryRegistry<ICoolableRecipe> coolableRegistry = new SingleRegistry<>();

    @Override
    public IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
        return new HeatableRecipe(in, out, heatFrom, heatTo);
    }

    @Override
    public ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
        return new CoolableRecipe(in, out, heatFrom, heatTo);
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
    public IRefineryRegistry<IDistillationRecipe> getDistillationRegistry() {
        return distillationRegistry;
    }

    private static class SingleRegistry<R extends IRefineryRecipe> implements IRefineryRegistry<R> {
        private final List<R> allRecipes = new LinkedList<>();

        @Override
        public Stream<R> getRecipes(Predicate<R> filter) {
            return allRecipes.stream().filter(filter);
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
                if (toRemove.test(recipe)) {
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
        private final FluidStack out;
        private final int heatFrom, heatTo;

        public HeatExchangeRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(in);
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
        public HeatableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(in, out, heatFrom, heatTo);
        }
    }

    public static class CoolableRecipe extends HeatExchangeRecipe implements ICoolableRecipe {
        public CoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(in, out, heatFrom, heatTo);
        }
    }
}
