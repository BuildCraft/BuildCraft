/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.inventory;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidHandlerCopy implements IFluidHandler {

	private IFluidHandler orignal;
	private FluidTankInfo[] contents;

	public FluidHandlerCopy(IFluidHandler orignal) {
		this.orignal = orignal;

		FluidTankInfo[] originalInfo = orignal.getTankInfo(ForgeDirection.UNKNOWN);

		contents = new FluidTankInfo[originalInfo.length];

		for (int i = 0; i < contents.length; i++) {
			if (originalInfo[i] != null) {
				if (originalInfo[i].fluid != null) {
					contents[i] = new FluidTankInfo(originalInfo[i].fluid.copy(), originalInfo[i].capacity);
				} else {
					contents[i] = new FluidTankInfo(null, originalInfo[i].capacity);
				}
			}
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return orignal.canFill(from, fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return orignal.canDrain(from, fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return contents;
	}
}
