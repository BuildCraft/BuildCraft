package buildcraft.transport.pipes.events;

import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.transport.Pipe;

public abstract class PipeEventFluid extends PipeEvent {
	public final FluidStack fluidStack;

	public PipeEventFluid(Pipe pipe, FluidStack fluidStack) {
		super(pipe);
		this.fluidStack = fluidStack;
	}

	public static class FindDest extends PipeEventFluid {
		public final List<ForgeDirection> destinations;

		public FindDest(Pipe pipe, FluidStack fluidStack, List<ForgeDirection> destinations) {
			super(pipe, fluidStack);
			this.destinations = destinations;
		}
	}
}
