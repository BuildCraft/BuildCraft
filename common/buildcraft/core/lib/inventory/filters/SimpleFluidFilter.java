/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory.filters;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.IFluidFilter;

public class SimpleFluidFilter implements IFluidFilter {

    private FluidStack fluidChecked;

    public SimpleFluidFilter(FluidStack stack) {
        if (stack != null) {
            fluidChecked = stack;
        }
    }

    @Override
    public boolean matches(FluidStack fluid) {
        if (fluidChecked != null) {
            return fluidChecked.isFluidEqual(fluid);
        } else {
            return fluid == null;
        }
    }
}
