package ct.buildcraft.compat;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.core.BCCoreItems;
import ct.buildcraft.core.item.ItemFragileFluidContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class Item2FluidCapWrapper implements IFluidHandler{

	final IItemHandler handler;
	
	public Item2FluidCapWrapper(IItemHandler handler) {
		this.handler = handler;
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public @NotNull FluidStack getFluidInTank(int tank) {
		return FluidStack.EMPTY;
	}

	@Override
	public int getTankCapacity(int tank) {
		return tank == 0 ? 500 : 0;
	}

	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
		return !handler.getStackInSlot(0).isEmpty();
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if(handler.getStackInSlot(0).isEmpty()) {
			int amount = Math.max(resource.getAmount(), 500);
			if(action.execute()) {
				ItemStack stack = new ItemStack(BCCoreItems.FRAGILE_FLUID_SHARD.get());
				FluidStack fluid = new FluidStack(resource.getFluid(), amount);
				ItemFragileFluidContainer.setFluid(stack, fluid);
				handler.insertItem(0, stack, false);
			}
			return amount;
		}
		return 0;
	}

	@Override
	public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
		return FluidStack.EMPTY;
	}

	@Override
	public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
		return FluidStack.EMPTY;
	}

}
