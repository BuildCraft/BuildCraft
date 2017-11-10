/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.inventory.filters;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class SimpleFluidFilter implements IFluidFilter {

	private Fluid fluidChecked;

	public SimpleFluidFilter(FluidStack stack) {
		if (stack != null) {
			fluidChecked = stack.getFluid();
		}
	}

	@Override
	public boolean matches(Fluid fluid) {
		if (fluidChecked != null) {
			return fluidChecked.getID() == fluid.getID();
		} else {
			return false;
		}
	}
}
