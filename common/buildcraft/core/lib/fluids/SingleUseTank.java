/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.fluids;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class SingleUseTank extends Tank {

	private Fluid acceptedFluid;

	public SingleUseTank(String name, int capacity, TileEntity tile) {
		super(name, capacity, tile);
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (resource == null) {
			return 0;
		}

		if (doFill && acceptedFluid == null) {
			acceptedFluid = resource.getFluid();
		}

		if (acceptedFluid == null || acceptedFluid == resource.getFluid()) {
			return super.fill(resource, doFill);
		}

		return 0;
	}

	public void reset() {
		acceptedFluid = null;
	}

	public void setAcceptedFluid(Fluid fluid) {
		this.acceptedFluid = fluid;
	}

	public Fluid getAcceptedFluid() {
		return acceptedFluid;
	}

	@Override
	public void writeTankToNBT(NBTTagCompound nbt) {
		super.writeTankToNBT(nbt);
		if (acceptedFluid != null) {
			nbt.setString("acceptedFluid", acceptedFluid.getName());
		}
	}

	@Override
	public void readTankFromNBT(NBTTagCompound nbt) {
		super.readTankFromNBT(nbt);
		acceptedFluid = FluidRegistry.getFluid(nbt.getString("acceptedFluid"));
	}
}
