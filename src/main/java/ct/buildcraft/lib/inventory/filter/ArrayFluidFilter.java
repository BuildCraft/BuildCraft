/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.inventory.filter;

import java.util.Optional;

import ct.buildcraft.api.core.IFluidFilter;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

/** Returns true if the stack matches any one one of the filter stacks. */
public class ArrayFluidFilter implements IFluidFilter {

    protected FluidStack[] fluids;

/*    public ArrayFluidFilter(ItemStack... stacks) {
//        this(StackUtil.listOf(stacks));
    	this()
    }*/

    public ArrayFluidFilter(FluidStack... iFluids) {
        fluids = iFluids;
    }

    public ArrayFluidFilter(NonNullList<ItemStack> stacks) {
        fluids = new FluidStack[stacks.size()];

        for (int i = 0; i < stacks.size(); ++i) {
            Optional<FluidStack> optional = FluidUtil.getFluidContained(stacks.get(i));
            if (optional.isPresent()) {
                fluids[i] = optional.get();
            }
            else
            	return;
        }
    }

    public boolean hasFilter() {
        for (FluidStack filter : fluids) {
            if (filter != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(FluidStack fluid) {
        for (FluidStack filter : fluids) {
            if (filter != null && filter.isFluidEqual(fluid)) {
                return true;
            }
        }

        return false;
    }
}
