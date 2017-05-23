/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TankProperties implements IFluidTankProperties {
    private final Tank tank;
    private final boolean canFill, canDrain;

    public TankProperties(Tank tank, boolean canFill, boolean canDrain) {
        this.tank = tank;
        this.canFill = canFill;
        this.canDrain = canDrain;
    }

    @Override
    public FluidStack getContents() {
        FluidStack current = tank.getFluid();
        return current == null ? null : current.copy();
    }

    @Override
    public int getCapacity() {
        return tank.getCapacity();
    }

    @Override
    public boolean canFill() {
        return canFill;
    }

    @Override
    public boolean canDrain() {
        return canDrain;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluidStack) {
        return canFill() && tank.canFillFluidType(fluidStack);
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluidStack) {
        return canDrain();
    }
}
