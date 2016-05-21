package buildcraft.transport.pipes.events;

import com.google.common.collect.Multiset;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.transport.Pipe;

public abstract class PipeEventFluid extends PipeEvent {
    public final FluidStack fluidStack;

    public PipeEventFluid(Pipe<?> pipe, FluidStack fluidStack) {
        super(pipe);
        this.fluidStack = fluidStack;
    }

    public static class FindDest extends PipeEventFluid {
        public final Multiset<EnumFacing> destinations;

        public FindDest(Pipe<?> pipe, FluidStack fluidStack, Multiset<EnumFacing> destinations) {
            super(pipe, fluidStack);
            this.destinations = destinations;
        }
    }
}
