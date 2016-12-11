package buildcraft.lib.net.cache;

import java.io.IOException;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.lib.net.PacketBufferBC;

public class NetworkedFluidStackCache extends NetworkedObjectCache<FluidStack> {
    private static final int FLUID_AMOUNT = 1;

    public NetworkedFluidStackCache() {
        // Use water for our base stack as it might not be too bad of an assumption
        super(new FluidStack(FluidRegistry.WATER, FLUID_AMOUNT));
    }

    @Override
    protected FluidStack getCanonical(FluidStack obj) {
        obj = obj.copy();
        obj.amount = FLUID_AMOUNT;
        return obj;
    }

    @Override
    protected void writeObject(FluidStack obj, PacketBufferBC buffer) {
        Fluid f = obj.getFluid();
        buffer.writeString(FluidRegistry.getFluidName(f));
        if (obj.tag == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeNBTTagCompoundToBuffer(obj.tag);
        }
    }

    @Override
    protected FluidStack readObject(PacketBufferBC buffer) throws IOException {
        Fluid fluid = FluidRegistry.getFluid(buffer.readStringFromBuffer(255));
        FluidStack stack = new FluidStack(fluid, FLUID_AMOUNT);
        if (buffer.readBoolean()) {
            stack.tag = buffer.readNBTTagCompoundFromBuffer();
        }
        return stack;
    }

    @Override
    protected String getCacheName() {
        return "FluidStack";
    }
}
