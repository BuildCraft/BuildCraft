package buildcraft.core.utils;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;

public final class FluidUtils {
	private FluidUtils () {

	}

	public static FluidStack getFluidStackFromItemStack(ItemStack stack) {
		if (stack != null) {
			if (stack.getItem() instanceof IFluidContainerItem) {
				IFluidContainerItem ctr = (IFluidContainerItem) stack.getItem();
				if (ctr.getFluid(stack) != null) {
					return ctr.getFluid(stack);
				}
			} else if (FluidContainerRegistry.isFilledContainer(stack) &&
					FluidContainerRegistry.getFluidForFilledItem(stack) != null) {
				return FluidContainerRegistry.getFluidForFilledItem(stack);
			} else if (stack.getItem() instanceof ItemBlock) {
				Block b = Block.getBlockFromItem(stack.getItem());
				if (b instanceof IFluidBlock) {
					return new FluidStack(((IFluidBlock) b).getFluid(), 1000);
				}
			}
		}
		return null;
	}

	public static Fluid getFluidFromItemStack(ItemStack stack) {
		return getFluidStackFromItemStack(stack).getFluid();
	}

	public static boolean isFluidContainer(ItemStack stack) {
		return stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isFilledContainer(stack);
	}
}
