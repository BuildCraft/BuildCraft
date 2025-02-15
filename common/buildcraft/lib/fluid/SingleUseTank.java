/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class SingleUseTank extends Tank {

    private static final String NBT_ACCEPTED_FLUID = "acceptedFluid";

    private FluidStack acceptedFluid;

    public SingleUseTank(@Nonnull String name, int capacity, BlockEntity tile) {
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
    public void writeTankToNBT(CompoundTag nbt) {
        super.writeTankToNBT(nbt);
        if (acceptedFluid != null) {
            nbt.put(NBT_ACCEPTED_FLUID, acceptedFluid.writeToNBT(new CompoundTag()));
        }
    }

    @Override
    public void readTankFromNBT(CompoundTag nbt) {
        super.readTankFromNBT(nbt);
        if (nbt.contains(NBT_ACCEPTED_FLUID, Tag.TAG_STRING)) {
            ResourceLocation fluidName = new ResourceLocation(nbt.getString(NBT_ACCEPTED_FLUID));
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
            setAcceptedFluid(fluid);
        } else {
            acceptedFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound(NBT_ACCEPTED_FLUID));
        }
    }
}
