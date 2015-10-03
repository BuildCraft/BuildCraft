/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidHandlerCopy implements IFluidHandler {

    private IFluidHandler orignal;
    private FluidTankInfo[] contents;

    public FluidHandlerCopy(IFluidHandler orignal) {
        this.orignal = orignal;

        FluidTankInfo[] originalInfo = orignal.getTankInfo(null);

        contents = new FluidTankInfo[originalInfo.length];

        for (int i = 0; i < contents.length; i++) {
            if (originalInfo[i] != null) {
                if (originalInfo[i].fluid != null) {
                    contents[i] = new FluidTankInfo(originalInfo[i].fluid.copy(), originalInfo[i].capacity);
                } else {
                    contents[i] = new FluidTankInfo(null, originalInfo[i].capacity);
                }
            }
        }
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return orignal.canFill(from, fluid);
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return orignal.canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return contents;
    }
}
