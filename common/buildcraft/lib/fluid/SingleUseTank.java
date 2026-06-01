/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.block.entity.BlockEntity;

import net.minecraftforge.common.util.Constants;
import net.minecraft.fluid.Fluid;
import buildcraft.lib.compat.FluidRegistryBC;
import buildcraft.lib.compat.FluidStackBC;
import net.minecraft.nbt.NbtElement;

public class SingleUseTank extends Tank {

    private static final String NBT_ACCEPTED_FLUID = "acceptedFluid";

    private FluidStackBC acceptedFluid;

    public SingleUseTank(@Nonnull String name, int capacity, BlockEntity tile) {
        super(name, capacity, tile);
    }

    @Override
    public int fill(FluidStackBC resource, boolean doFill) {
        if (resource == null) {
            return 0;
        }

        if (doFill && acceptedFluid == null) {
            acceptedFluid = resource.copy();
            acceptedFluid.amount = 1;
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
            this.acceptedFluid = new FluidStackBC(fluid, 1);
        }
    }

    public void setAcceptedFluid(FluidStackBC fluid) {
        if (fluid == null) {
            this.acceptedFluid = null;
        } else {
            this.acceptedFluid = new FluidStackBC(fluid, 1);
        }
    }

    public FluidStackBC getAcceptedFluid() {
        return acceptedFluid;
    }

    @Override
    public void writeTankToNBT(NbtCompound nbt) {
        super.writeTankToNBT(nbt);
        if (acceptedFluid != null) {
            nbt.put(NBT_ACCEPTED_FLUID, acceptedFluid.writeToNBT(new NbtCompound()));
        }
    }

    @Override
    public void readTankFromNBT(NbtCompound nbt) {
        super.readTankFromNBT(nbt);
        if (nbt.contains(NBT_ACCEPTED_FLUID, NbtElement.STRING_TYPE)) {
            setAcceptedFluid(FluidRegistryBC.getFluid(nbt.getString(NBT_ACCEPTED_FLUID)));
        } else {
            acceptedFluid = FluidStackBC.loadFluidStackFromNBT(nbt.getCompound(NBT_ACCEPTED_FLUID));
        }
    }
}
