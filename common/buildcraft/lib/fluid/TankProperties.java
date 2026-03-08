/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import buildcraft.lib.misc.StackUtil;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraftforge.fluids.FluidStack;

//public class TankProperties implements FluidTankProperties
public class TankProperties {
    private final Tank tank;
    private final boolean canFill, canDrain;

    public TankProperties(Tank tank, boolean canFill, boolean canDrain) {
        this.tank = tank;
        this.canFill = canFill;
        this.canDrain = canDrain;
    }

    // @Override
    public FluidStack getContents() {
        FluidStack current = tank.getFluid();
//        return current == null ? null : current.copy();
        return (current == null || current.getRawFluid() instanceof EmptyFluid) ? StackUtil.EMPTY_FLUID : current.copy();
    }

    // @Override
    public int getCapacity() {
        return tank.getCapacity();
    }

    // @Override
    public boolean canFill() {
        return canFill;
    }

    // @Override
    public boolean canDrain() {
        return canDrain;
    }

    // @Override
    public boolean canFillFluidType(FluidStack fluidStack) {
//        return canFill() && tank.canFillFluidType(fluidStack);
        return canFill() && tank.isFluidValid(fluidStack);
    }

    // @Override
    public boolean canDrainFluidType(FluidStack fluidStack) {
        return canDrain();
    }
}
