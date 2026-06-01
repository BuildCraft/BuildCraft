// STUB(R.Chen): Forge FluidTankProperties — compile shim.
package net.minecraftforge.fluids.capability;

import buildcraft.lib.compat.FluidStackBC;

public class FluidTankProperties implements IFluidTankProperties {
    private final FluidStackBC contents;
    private final int capacity;
    private final boolean canFill;
    private final boolean canDrain;

    public FluidTankProperties(FluidStackBC contents, int capacity) {
        this(contents, capacity, true, true);
    }

    public FluidTankProperties(FluidStackBC contents, int capacity, boolean canFill, boolean canDrain) {
        this.contents = contents;
        this.capacity = capacity;
        this.canFill = canFill;
        this.canDrain = canDrain;
    }

    @Override public FluidStackBC getContents() { return contents; }
    @Override public int getCapacity() { return capacity; }
    @Override public boolean canFill() { return canFill; }
    @Override public boolean canDrain() { return canDrain; }
}
