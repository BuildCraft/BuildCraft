/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory.filter;

import ct.buildcraft.api.core.IFluidFilter;

import net.minecraftforge.fluids.FluidStack;

/** Returns true if the stack matches any one one of the filter stacks. */
public class PassThroughFluidFilter implements IFluidFilter {

    @Override
    public boolean matches(FluidStack fluid) {
        return fluid != null;
    }

}
