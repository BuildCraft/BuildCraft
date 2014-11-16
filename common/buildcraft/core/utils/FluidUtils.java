package buildcraft.core.utils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

public final class FluidUtils {
	private FluidUtils () {

	}

	public static Fluid getFluidFromItemStack(ItemStack stack) {
		if (stack != null) {
			if (stack.getItem() instanceof IFluidContainerItem) {
				IFluidContainerItem ctr = (IFluidContainerItem) stack.getItem();
				if (ctr.getFluid(stack) != null) {
					return ctr.getFluid(stack).getFluid();
				}
			} else if (FluidContainerRegistry.isFilledContainer(stack) &&
					FluidContainerRegistry.getFluidForFilledItem(stack) != null) {
				return FluidContainerRegistry.getFluidForFilledItem(stack).getFluid();
			}
		}
		return null;
	}

	public static boolean isFluidContainer(ItemStack stack) {
		return stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isFilledContainer(stack);
	}
}
