package buildcraft.transport.utils;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidRenderData {
	public int fluidID, amount, color;

	public FluidRenderData(int fluidID, int amount, int color) {
		this.fluidID = fluidID;
		this.amount = amount;
		this.color = color;
	}

	public FluidRenderData(FluidStack stack) {
		this(stack.getFluid().getID(), stack.amount, stack.getFluid().getColor(stack));
	}

	public FluidStack getFluidStack() {
		Fluid fluid = FluidRegistry.getFluid(fluidID);
		if (fluid != null) {
			return new FluidStack(fluid, amount);
		} else {
			return null;
		}
	}
}
