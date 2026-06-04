/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import java.util.List;

import buildcraft.lib.compat.FluidStackBC;
import net.minecraftforge.fluids.FluidUtil;

import buildcraft.api.core.IFluidFilter;

import buildcraft.lib.misc.StackUtil;

/** Returns true if the stack matches any one one of the filter stacks. */
public class ArrayFluidFilter implements IFluidFilter {

    protected FluidStackBC[] fluids;

    public ArrayFluidFilter(ItemStack... stacks) {
        this(StackUtil.listOf(stacks));
    }

    public ArrayFluidFilter(FluidStackBC... iFluids) {
        fluids = iFluids;
    }

    public ArrayFluidFilter(DefaultedList<ItemStack> stacks) {
        fluids = new FluidStackBC[stacks.size()];

        for (int i = 0; i < stacks.size(); ++i) {
            FluidStackBC stack = FluidUtil.getFluidContained(stacks.get(i));
            if (stack != null) {
                fluids[i] = stack;
            }
        }
    }

    public boolean hasFilter() {
        for (FluidStackBC filter : fluids) {
            if (filter != null) {
                return true;
            }
        }
        return false;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public boolean matches(FluidStackBC fluid) {
        for (FluidStackBC filter : fluids) {
            if (filter != null && filter.isFluidEqual(fluid)) {
                return true;
            }
        }

        return false;
    }
    @Override
    public boolean matches(net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant fluid, long amount) {
        return matches(buildcraft.lib.compat.FluidStackBC.of(fluid, amount));
    }
}