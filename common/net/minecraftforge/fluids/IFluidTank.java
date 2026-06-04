// STUB(R.Chen): Forge IFluidTank — compile shim.
package net.minecraftforge.fluids;

import buildcraft.lib.compat.FluidStackBC;

public interface IFluidTank {
    FluidStackBC getFluid();
    int getFluidAmount();
    int getCapacity();
    boolean canFillFluidType(FluidStackBC fluid);
    int fill(FluidStackBC resource, boolean doFill);
    FluidStackBC drain(int maxDrain, boolean doDrain);
}
