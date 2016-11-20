package buildcraft.lib.inventory.filter;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.IFluidFilter;

public class InvertedFluidFilter implements IFluidFilter {

    public final IFluidFilter delegate;

    public InvertedFluidFilter(IFluidFilter delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean matches(FluidStack fluid) {
        return !delegate.matches(fluid);
    }
}
