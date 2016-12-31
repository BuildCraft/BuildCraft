/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.inventory.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import buildcraft.api.core.IFluidFilter;

import buildcraft.lib.misc.StackUtil;

/** Returns true if the stack matches any one one of the filter stacks. */
public class ArrayFluidFilter implements IFluidFilter {

    protected FluidStack[] fluids;

    public ArrayFluidFilter(ItemStack... stacks) {
        this(StackUtil.listOf(stacks));
    }

    public ArrayFluidFilter(FluidStack... iFluids) {
        fluids = iFluids;
    }

    public ArrayFluidFilter(NonNullList<ItemStack> stacks) {
        fluids = new FluidStack[stacks.size()];

        for (int i = 0; i < stacks.size(); ++i) {
            FluidStack stack = FluidUtil.getFluidContained(stacks.get(i));
            if (stack != null) {
                fluids[i] = stack;
            }
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
