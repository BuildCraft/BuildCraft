/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.fluids;

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

    public String getDebugString() {
        return getFluidAmount() + " / " + capacity + " MB of " + (getFluid() != null ? getFluid().getFluid().getName() : "n/a");
    }
}
