// STUB(R.Chen): Forge IFluidTankProperties — compile shim.
package net.minecraftforge.fluids.capability;

import buildcraft.lib.compat.FluidStackBC;

public interface IFluidTankProperties {
    FluidStackBC getContents();
    int getCapacity();
    boolean canFill();
    boolean canDrain();
}
