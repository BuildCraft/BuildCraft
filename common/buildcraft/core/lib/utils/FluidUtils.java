package buildcraft.core.lib.utils;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
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
				return ctr.getFluid(stack);
			} else if (FluidContainerRegistry.isFilledContainer(stack)) {
				return FluidContainerRegistry.getFluidForFilledItem(stack);
			} else if (stack.getItem() instanceof ItemBlock) {
				Block b = Block.getBlockFromItem(stack.getItem());
				if (b != null) {
					if (b instanceof IFluidBlock && ((IFluidBlock) b).getFluid() != null) {
						return new FluidStack(((IFluidBlock) b).getFluid(), 1000);
					} else if (b == Blocks.lava) {
						return new FluidStack(FluidRegistry.getFluid("lava"), 1000);
					} else if (b == Blocks.water) {
						return new FluidStack(FluidRegistry.getFluid("water"), 1000);
					}
				}
			}
		}
		return null;
	}

	public static Fluid getFluidFromItemStack(ItemStack stack) {
		FluidStack fluidStack = getFluidStackFromItemStack(stack);
		return fluidStack != null ? fluidStack.getFluid() : null;
	}

	public static boolean isFluidContainer(ItemStack stack) {
		return stack != null && stack.getItem() != null && (stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isFilledContainer(stack));
	}
}
