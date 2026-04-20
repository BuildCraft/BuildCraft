/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ct.buildcraft.lib.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class SingleUseTank extends FluidTank {

    private static final String NBT_ACCEPTED_FLUID = "acceptedFluid";

    private FluidStack acceptedFluid;

    public SingleUseTank(int capacity) {
        super(capacity);
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        if (resource == null) {
            return 0;
        }

        if (doFill.execute() && acceptedFluid == null) {
            acceptedFluid = resource.copy();
            acceptedFluid.setAmount(1);
        }

        if (acceptedFluid == null || acceptedFluid.isFluidEqual(resource)) {
            return super.fill(resource, doFill);
        }

        return 0;
    }

    public void reset() {
        acceptedFluid = null;
    }

    public void setAcceptedFluid(Fluid fluid) {
        if (fluid == null) {
            this.acceptedFluid = null;
        } else {
            this.acceptedFluid = new FluidStack(fluid, 1);
        }
    }

    public void setAcceptedFluid(FluidStack fluid) {
        if (fluid == null) {
            this.acceptedFluid = null;
        } else {
            this.acceptedFluid = new FluidStack(fluid, 1);
        }
    }

    public FluidStack getAcceptedFluid() {
        return acceptedFluid;
    }

    @Override
	public FluidTank readFromNBT(CompoundTag nbt) {
    	acceptedFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound(NBT_ACCEPTED_FLUID));
		return super.readFromNBT(nbt);
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag nbt) {
		if (acceptedFluid != null) {
            nbt.put(NBT_ACCEPTED_FLUID, acceptedFluid.writeToNBT(new CompoundTag()));
        }
		return super.writeToNBT(nbt);

	}

}
