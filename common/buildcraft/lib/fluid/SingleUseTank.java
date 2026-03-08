/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class SingleUseTank extends Tank {

    private static final String NBT_ACCEPTED_FLUID = "acceptedFluid";

    private FluidStack acceptedFluid;

    public SingleUseTank(@Nonnull String name, int capacity, TileEntity tile) {
        super(name, capacity, tile);
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
    public void writeTankToNBT(CompoundNBT nbt) {
        super.writeTankToNBT(nbt);
        if (acceptedFluid != null) {
            nbt.put(NBT_ACCEPTED_FLUID, acceptedFluid.writeToNBT(new CompoundNBT()));
        }
    }

    @Override
    public void readTankFromNBT(CompoundNBT nbt) {
        super.readTankFromNBT(nbt);
        if (nbt.contains(NBT_ACCEPTED_FLUID, Constants.NBT.TAG_STRING)) {
            ResourceLocation fluidName = new ResourceLocation(nbt.getString(NBT_ACCEPTED_FLUID));
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
            setAcceptedFluid(fluid);
        } else {
            acceptedFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound(NBT_ACCEPTED_FLUID));
        }
    }
}
