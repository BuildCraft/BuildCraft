package buildcraft.core.lib.utils;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.*;

/**
 * The methods in this class are useless (but left in as code that needs to be ported still exists)
 */
@Deprecated
public final class FluidUtils {
    private FluidUtils() {

    }

    public static Fluid getFluidFromItemStack(ItemStack stack) {
        FluidStack fluidStack = FluidUtil.getFluidContained(stack);
        return fluidStack != null ? fluidStack.getFluid() : null;
    }

    public static boolean isFluidContainer(ItemStack stack) {
        return stack != null && stack.getItem() != null && (stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry
                .isFilledContainer(stack));
    }
}
