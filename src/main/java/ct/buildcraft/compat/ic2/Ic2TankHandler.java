package ct.buildcraft.compat.ic2;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Fluids.InternalFluidTank;
import ic2.core.fluid.Ic2FluidStack;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class Ic2TankHandler implements IFluidHandler{

	protected final Fluids tank;
	protected final  List<InternalFluidTank> tanks;
	private Direction face;
	
	
	public Ic2TankHandler(Fluids tank, Direction face) {
		this.tank = tank;
		this.tanks = ((List<InternalFluidTank>)tank.getAllTanks());
		this.face = face;
	}

	@Override
	public int getTanks() {
		return tanks.size();
	}

	@Override
	public @NotNull FluidStack getFluidInTank(int tankid) {
		Ic2FluidStack ic2FluidStack = tanks.get(tankid).getFluidStack();
		return new FluidStack(ic2FluidStack.getFluid(), ic2FluidStack.getAmountMb());
	}

	@Override
	public int getTankCapacity(int tankid) {
		return tanks.get(tankid).getCapacity();
	}

	@Override
	public boolean isFluidValid(int tankid, @NotNull FluidStack stack) {
		return tanks.get(tankid).fillMb(Ic2FluidStack.create(stack.getFluid(), stack.getAmount()), true) > 0;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return tank.fillMb(face, Ic2FluidStack.create(resource.getFluid(), resource.getAmount()), action.simulate());
	}

	@Override
	public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
		int drainAmount = tank.drainMb(face, Ic2FluidStack.create(resource.getFluid(), resource.getAmount()), action.simulate());
		return drainAmount > 0 ? new FluidStack(resource.getFluid(), drainAmount) : FluidStack.EMPTY;
	}

	@Override
	public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
		Ic2FluidStack drainFluid = tank.drainMb(face, maxDrain, action.simulate());
		return drainFluid.isEmpty() ? FluidStack.EMPTY : new FluidStack(drainFluid.getFluid(), drainFluid.getAmountMb());
	}

}
