/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import buildcraft.lib.compat.FluidStackBC;
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
    public FluidStackBC getContents() {
        FluidStackBC current = tank.getFluid();
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

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean canFillFluidType(FluidStackBC fluidStack) {
        return canFill() && tank.canFillFluidType(fluidStack);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean canDrainFluidType(FluidStackBC fluidStack) {
        return canDrain();
    }
}
