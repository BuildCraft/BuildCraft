/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.fluid;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ct.buildcraft.api.fuels.IFuel;
import ct.buildcraft.api.fuels.IFuelManager;
import net.minecraftforge.fluids.FluidStack;

public enum FuelRegistry implements IFuelManager {
    INSTANCE;

    private final List<IFuel> fuels = new LinkedList<>();

    @Override
    public <F extends IFuel> F addFuel(F fuel) {
        fuels.add(fuel);
        return fuel;
    }

    @Override
    public IFuel addFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
        return addFuel(new Fuel(fluid, powerPerCycle, totalBurningTime));
    }

    @Override
    public IDirtyFuel addDirtyFuel(FluidStack fuel, long powerPerCycle, int totalBurningTime, FluidStack residue) {
        return addFuel(new DirtyFuel(fuel, powerPerCycle, totalBurningTime, residue));
    }

    @Override
    public Collection<IFuel> getFuels() {
        return fuels;
    }

    @Override
    public IFuel getFuel(FluidStack fluid) {
        if (fluid == null) {
            return null;
        }
        for (IFuel fuel : fuels) {
            if (fuel.getFluid().isFluidEqual(fluid)) {
                return fuel;
            }
        }
        return null;
    }

    public static class Fuel implements IFuel {
        private final FluidStack fluid;
        private final long powerPerCycle;
        private final int totalBurningTime;

        public Fuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
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
    }

    public static class DirtyFuel extends Fuel implements IDirtyFuel {

        private final FluidStack residue;

        public DirtyFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime, FluidStack residue) {
            super(fluid, powerPerCycle, totalBurningTime);
            this.residue = residue;
        }

        @Override
        public FluidStack getResidue() {
            return residue;
        }
    }
}
