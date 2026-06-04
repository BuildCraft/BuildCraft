// STUB(R.Chen): Forge FluidTank — compile shim. TODO: replace with Tank from lib/fluid/Tank.java.
package net.minecraftforge.fluids;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

import buildcraft.lib.compat.FluidStackBC;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidTank implements IFluidTank, IFluidHandler {
    protected FluidStackBC fluid;
    protected int capacity;
    /** STUB(R.Chen): tile reference for onContentsChanged callbacks — mirrors Forge FluidTank. */
    protected BlockEntity tile;

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
            onContentsChanged();
        }
        return filled;
    }

    @Override
    public FluidStackBC drain(int maxDrain, boolean doDrain) {
        if (fluid == null || fluid.isEmpty()) return null;
        int drained = (int) Math.min(maxDrain, fluid.getAmount());
        FluidStackBC stack = new FluidStackBC(fluid.getFluid(), drained);
        if (doDrain) { fluid.setAmount(fluid.getAmount() - drained); onContentsChanged(); }
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

    /** Called when tank contents change; override to react. */
    protected void onContentsChanged() {}

    // STUB(R.Chen): NBT serialization — mirrors Forge FluidTank read/writeToNBT.
    public NbtCompound writeToNBT(NbtCompound nbt) {
        if (fluid != null && !fluid.isEmpty()) {
            NbtCompound fluidTag = new NbtCompound();
            fluid.writeToNBT(fluidTag);
            nbt.put("Fluid", fluidTag);
        }
        nbt.putInt("Capacity", capacity);
        return nbt;
    }

    public FluidTank readFromNBT(NbtCompound nbt) {
        if (nbt.contains("Fluid")) {
            fluid = FluidStackBC.loadFluidStackFromNBT(nbt.getCompound("Fluid"));
        } else {
            fluid = null;
        }
        if (nbt.contains("Capacity")) {
            capacity = nbt.getInt("Capacity");
        }
        return this;
    }
}
