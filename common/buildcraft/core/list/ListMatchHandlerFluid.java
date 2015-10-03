package buildcraft.core.list;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.FluidUtils;

public class ListMatchHandlerFluid extends ListMatchHandler {
	@Override
	public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
		if (type == Type.TYPE) {
			if (FluidContainerRegistry.isContainer(stack) && FluidContainerRegistry.isContainer(target)) {
				ItemStack emptyContainerStack = FluidContainerRegistry.drainFluidContainer(stack);
				ItemStack emptyContainerTarget = FluidContainerRegistry.drainFluidContainer(target);
				if (StackHelper.isMatchingItem(emptyContainerStack, emptyContainerTarget, true, true)) {
					return true;
				}
			}
		} else if (type == Type.MATERIAL) {
			FluidStack fStack = FluidUtils.getFluidStackFromItemStack(stack);
			FluidStack fTarget = FluidUtils.getFluidStackFromItemStack(target);
			if (fStack != null && fTarget != null) {
				return fStack.isFluidEqual(fTarget);
			}
		}
		return false;
	}

	@Override
	public boolean isValidSource(Type type, ItemStack stack) {
		if (type == Type.TYPE) {
			return FluidContainerRegistry.isContainer(stack);
		} else if (type == Type.MATERIAL) {
			return FluidUtils.getFluidStackFromItemStack(stack) != null;
		}
		return false;
	}

	@Override
	public List<ItemStack> getClientExamples(Type type, ItemStack stack) {
		if (type == Type.MATERIAL) {
			FluidStack fStack = FluidUtils.getFluidStackFromItemStack(stack);
			if (fStack != null) {
				List<ItemStack> examples = new ArrayList<ItemStack>();
				for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
					if (fStack.isFluidEqual(data.fluid)) {
						examples.add(data.filledContainer);
					}
				}
				return examples;
			}
		} else if (type == Type.TYPE) {
			if (FluidContainerRegistry.isContainer(stack)) {
				List<ItemStack> examples = new ArrayList<ItemStack>();
				ItemStack emptyContainerStack = FluidContainerRegistry.drainFluidContainer(stack);
				examples.add(stack);
				examples.add(emptyContainerStack);
				for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
					if (StackHelper.isMatchingItem(data.emptyContainer, emptyContainerStack, true, true)) {
						examples.add(data.filledContainer);
					}
				}
				return examples;
			}
		}
		return null;
	}
}
