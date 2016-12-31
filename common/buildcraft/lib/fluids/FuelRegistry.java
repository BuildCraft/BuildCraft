package buildcraft.lib.fluids;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager;

public enum FuelRegistry implements IFuelManager {
    INSTANCE;

    private final List<IFuel> fuels = new LinkedList<>();

    @Override
    public IFuel addFuel(IFuel fuel) {
        fuels.add(fuel);
        return fuel;
    }

    @Override
    public IFuel addFuel(Fluid fluid, long powerPerCycle, int totalBurningTime) {
        return addFuel(new Fuel(fluid, powerPerCycle, totalBurningTime));
    }

    @Override
    public IDirtyFuel addDirtyFuel(Fluid fuel, long powerPerCycle, int totalBurningTime, FluidStack residue) {
        IDirtyFuel dirty = new DirtyFuel(fuel, powerPerCycle, totalBurningTime, residue);
        addFuel(dirty);
        return dirty;
    }

    @Override
    public Collection<IFuel> getFuels() {
        return fuels;
    }

    @Override
    public IFuel getFuel(Fluid fluid) {
        for (IFuel fuel : fuels) {
            if (fuel.getFluid() == fluid) {
                return fuel;
            }
        }
        return null;
    }

    public static class Fuel implements IFuel {
        private final Fluid fluid;
        private final long powerPerCycle;
        private final int totalBurningTime;

        public Fuel(Fluid fluid, long powerPerCycle, int totalBurningTime) {
            this.fluid = fluid;
            this.powerPerCycle = powerPerCycle;
            this.totalBurningTime = totalBurningTime;
        }

        @Override
        public Fluid getFluid() {
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

        public DirtyFuel(Fluid fluid, long powerPerCycle, int totalBurningTime, FluidStack residue) {
            super(fluid, powerPerCycle, totalBurningTime);
            this.residue = residue;
        }

        @Override
        public FluidStack getResidue() {
            return residue;
        }
    }
}
