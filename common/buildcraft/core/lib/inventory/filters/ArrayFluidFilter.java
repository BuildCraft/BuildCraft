/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.inventory.filters;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * Returns true if the stack matches any one one of the filter stacks.
 */
public class ArrayFluidFilter implements IFluidFilter {

	protected Fluid[] fluids;

	public ArrayFluidFilter(ItemStack... stacks) {
		fluids = new Fluid[stacks.length];

		for (int i = 0; i < stacks.length; ++i) {
			FluidStack stack = FluidContainerRegistry.getFluidForFilledItem(stacks[i]);
			if (stack != null) {
				fluids[i] = stack.getFluid();
			}
		}
	}

	public ArrayFluidFilter(Fluid... iFluids) {
		fluids = iFluids;
	}


	public boolean hasFilter() {
		for (Fluid filter : fluids) {
			if (filter != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean matches(Fluid fluid) {
		for (Fluid filter : fluids) {
			if (filter != null && fluid.getID() == filter.getID()) {
				return true;
			}
		}

		return false;
	}
}
