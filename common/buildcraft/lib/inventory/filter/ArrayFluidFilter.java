/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.inventory.filter;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import buildcraft.api.core.IFluidFilter;

/** Returns true if the stack matches any one one of the filter stacks. */
public class ArrayFluidFilter implements IFluidFilter {

    protected FluidStack[] fluids;

    public ArrayFluidFilter(ItemStack... stacks) {
        fluids = new FluidStack[stacks.length];

        for (int i = 0; i < stacks.length; ++i) {
            FluidStack stack = FluidUtil.getFluidContained(stacks[i]);
            if (stack != null) {
                fluids[i] = stack;
            }
        }
    }

    public ArrayFluidFilter(FluidStack... iFluids) {
        fluids = iFluids;
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
