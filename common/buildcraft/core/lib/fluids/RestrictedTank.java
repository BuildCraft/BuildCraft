/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.fluids;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class RestrictedTank extends Tank {

	private final Fluid[] acceptedFluids;

	public RestrictedTank(String name, int capacity, TileEntity tile, Fluid... acceptedFluids) {
		super(name, capacity, tile);
		this.acceptedFluids = acceptedFluids;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (!acceptsFluid(resource.getFluid())) {
			return 0;
		} else {
			return super.fill(resource, doFill);
		}
	}

	public boolean acceptsFluid(Fluid fluid) {
		for (Fluid accepted : acceptedFluids) {
			if (accepted.equals(fluid)) {
				return true;
			}
		}

		return false;
	}
}
