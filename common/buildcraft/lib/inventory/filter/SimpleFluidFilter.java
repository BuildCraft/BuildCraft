/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import buildcraft.lib.compat.FluidStackBC;

import buildcraft.api.core.IFluidFilter;

public class SimpleFluidFilter implements IFluidFilter {

    private FluidStackBC fluidChecked;

    public SimpleFluidFilter(FluidStackBC stack) {
        if (stack != null) {
            fluidChecked = stack;
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean matches(FluidStackBC fluid) {
        if (fluidChecked != null) {
            return fluidChecked.isFluidEqual(fluid);
        } else {
            return fluid == null;
        }
    }
    @Override
    public boolean matches(net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant fluid, long amount) {
        return matches(buildcraft.lib.compat.FluidStackBC.of(fluid, amount));
    }
}