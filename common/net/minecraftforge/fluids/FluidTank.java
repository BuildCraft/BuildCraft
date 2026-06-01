// STUB(R.Chen): Forge FluidTank — compile shim. TODO: replace with Tank from lib/fluid/Tank.java.
package net.minecraftforge.fluids;

import buildcraft.lib.compat.FluidStackBC;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidTank implements IFluidTank, IFluidHandler {
    protected FluidStackBC fluid;
    protected int capacity;

    public FluidTank(int capacity) { this.capacity = capacity; }

    @Override public FluidStackBC getFluid() { return fluid; }
    @Override public int getFluidAmount() { return fluid == null ? 0 : (int) fluid.getAmount(); }
    @Override public int getCapacity() { return capacity; }
    @Override public boolean canFillFluidType(FluidStackBC fluid) { return true; }

    @Override
    public int fill(FluidStackBC resource, boolean doFill) {
        if (resource == null || resource.isEmpty()) return 0;
        int filled = (int) Math.min(resource.getAmount(), capacity - getFluidAmount());
        if (doFill && filled > 0) {
            if (fluid == null || fluid.isEmpty()) fluid = resource.copy();
            else fluid.setAmount(fluid.getAmount() + filled);
        }
        return filled;
    }

    @Override
    public FluidStackBC drain(int maxDrain, boolean doDrain) {
        if (fluid == null || fluid.isEmpty()) return null;
        int drained = (int) Math.min(maxDrain, fluid.getAmount());
        FluidStackBC stack = new FluidStackBC(fluid.getFluid(), drained);
        if (doDrain) fluid.setAmount(fluid.getAmount() - drained);
        return stack;
    }

    @Override
    public FluidStackBC drain(FluidStackBC resource, boolean doDrain) {
        if (resource == null || fluid == null || !fluid.isFluidEqual(resource)) return null;
        return drain((int) resource.getAmount(), doDrain);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{ new net.minecraftforge.fluids.capability.FluidTankProperties(fluid, capacity) };
    }
}
