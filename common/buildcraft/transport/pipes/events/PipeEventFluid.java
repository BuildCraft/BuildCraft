package buildcraft.transport.pipes.events;

import java.util.List;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public abstract class PipeEventFluid extends PipeEvent {
	public final FluidStack fluidStack;

	public PipeEventFluid(FluidStack fluidStack) {
		this.fluidStack = fluidStack;
	}

	public static class FindDest extends PipeEventFluid {
		public final List<ForgeDirection> destinations;

		public FindDest(FluidStack fluidStack, List<ForgeDirection> destinations) {
			super(fluidStack);
			this.destinations = destinations;
		}
	}

	public static class Fill extends PipeEventFluid {
		public final ForgeDirection from;
		public final boolean doAdd;

		public Fill(ForgeDirection from, FluidStack fluidStack, boolean doAdd) {
			super(fluidStack);
			this.from = from;
			this.doAdd = doAdd;
		}
	}
}
