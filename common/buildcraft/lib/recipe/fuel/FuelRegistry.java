/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe.fuel;

import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager;
import com.google.common.collect.Lists;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public enum FuelRegistry implements IFuelManager {
    INSTANCE;

    // private final List<IFuel> fuels = new LinkedList<>();
    private final List<IFuel> unregisteredFuels = new LinkedList<>();

    @Override
//    public <F extends IFuel> F addFuel(F fuel)
    public <F extends IFuel> F addUnregisteredFuel(F fuel) {
//        fuels.add(fuel);
        unregisteredFuels.add(fuel);
        return fuel;
    }

    @Override
//    public IFuel addFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime)
    public IFuel addUnregisteredFuel(ResourceLocation id, FluidStack fluid, long powerPerCycle, int totalBurningTime) {
//        return addFuel(new Fuel(fluid, powerPerCycle, totalBurningTime));
        return addUnregisteredFuel(new Fuel(id, fluid, powerPerCycle, totalBurningTime));
    }

    @Override
//    public IDirtyFuel addDirtyFuel(FluidStack fuel, long powerPerCycle, int totalBurningTime, FluidStack residue)
    public IDirtyFuel addUnregisteredDirtyFuel(ResourceLocation id, FluidStack fuel, long powerPerCycle, int totalBurningTime, FluidStack residue) {
//        return addFuel(new DirtyFuel(fuel, powerPerCycle, totalBurningTime, residue));
        return addUnregisteredFuel(new DirtyFuel(id, fuel, powerPerCycle, totalBurningTime, residue));
    }

    @Override
//    public Collection<IFuel> getFuels()
    public Collection<IFuel> getFuels(World world) {
        Collection<IFuel> ret = Lists.newArrayList();
        ret.addAll(unregisteredFuels);
        world.getRecipeManager().byType(IFuel.TYPE).values().forEach(r -> ret.add((Fuel) r));
//        return fuels;
        return ret;
    }

    @Override
//    public IFuel getFuel(FluidStack fluid)
    public IFuel getFuel(World world, FluidStack fluid) {
        if (fluid == null) {
            return null;
        }
        Collection<IFuel> fuels = Lists.newArrayList();
        fuels.addAll(unregisteredFuels);
        world.getRecipeManager().byType(IFuel.TYPE).values().forEach(r -> fuels.add((Fuel) r));
        for (IFuel fuel : fuels) {
            if (fuel.getFluid().isFluidEqual(fluid)) {
                return fuel;
            }
        }
        return null;
    }

    public static class Fuel implements IFuel {
        private final ResourceLocation id;

        private final FluidStack fluid;
        private final long powerPerCycle;
        private final int totalBurningTime;

        // public Fuel(FluidStack fluid, long powerPerCycle, int totalBurningTime)
        public Fuel(ResourceLocation id, FluidStack fluid, long powerPerCycle, int totalBurningTime) {
            this.id = id;
            this.fluid = fluid;
            this.powerPerCycle = powerPerCycle;
            this.totalBurningTime = totalBurningTime;
        }

        @Override
        public FluidStack getFluid() {
            return fluid;
        }

        @Override
        public long getPowerPerCycle() {
            return powerPerCycle;
        }

        @Override
        public int getTotalBurningTime() {
            return totalBurningTime;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public IRecipeSerializer<IFuel> getSerializer() {
            return FuelRecipeSerializer.INSTANCE;
        }
    }

    public static class DirtyFuel extends Fuel implements IDirtyFuel {
        private final FluidStack residue;

        // public DirtyFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime, FluidStack residue)
        public DirtyFuel(ResourceLocation id, FluidStack fluid, long powerPerCycle, int totalBurningTime, FluidStack residue) {
//            super(fluid, powerPerCycle, totalBurningTime);
            super(id, fluid, powerPerCycle, totalBurningTime);
            this.residue = residue;
        }

        @Override
        public FluidStack getResidue() {
            return residue;
        }
    }
}
