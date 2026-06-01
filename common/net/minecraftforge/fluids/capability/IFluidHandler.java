// STUB(R.Chen): Forge IFluidHandler — compile shim for unmigrated files. TODO: migrate to Fabric Storage<FluidVariant>.
package net.minecraftforge.fluids.capability;

import buildcraft.lib.compat.FluidStackBC;

public interface IFluidHandler {
    IFluidTankProperties[] getTankProperties();
    int fill(FluidStackBC resource, boolean doFill);
    FluidStackBC drain(FluidStackBC resource, boolean doDrain);
    FluidStackBC drain(int maxDrain, boolean doDrain);
}
