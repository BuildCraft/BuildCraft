/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.fuels;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraftforge.fluids.Fluid;

import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager;

public final class FuelManager implements IFuelManager {
	public static final FuelManager INSTANCE = new FuelManager();

	private final List<IFuel> fuels = new LinkedList<IFuel>();

	private FuelManager() {
	}

	@Override
	public IFuel addFuel(IFuel fuel) {
		fuels.add(fuel);
		return fuel;
	}

	@Override
	public IFuel addFuel(Fluid fluid, int powerPerCycle, int totalBurningTime) {
		return addFuel(new BCFuel(fluid, powerPerCycle, totalBurningTime));
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

	private static final class BCFuel implements IFuel {
		private final Fluid fluid;
		private final int powerPerCycle;
		private final int totalBurningTime;

		public BCFuel(Fluid fluid, int powerPerCycle, int totalBurningTime) {
			this.fluid = fluid;
			this.powerPerCycle = powerPerCycle;
			this.totalBurningTime = totalBurningTime;
		}

		@Override
		public Fluid getFluid() {
			return fluid;
		}

		@Override
		public int getTotalBurningTime() {
			return totalBurningTime;
		}

		@Override
		public int getPowerPerCycle() {
			return powerPerCycle;
		}
	}
}
