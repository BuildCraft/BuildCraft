package buildcraft.lib.list;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import buildcraft.api.lists.ListMatchHandler;

import buildcraft.lib.misc.StackUtil;

import javax.annotation.Nonnull;

public class ListMatchHandlerFluid extends ListMatchHandler {
    private static final List<ItemStack> fluidHoldingItems = new ArrayList<>();

    public static void fmlPostInit() {
        for (Item item : Item.REGISTRY) {
            ItemStack toTry = new ItemStack(item);
            IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(toTry);
            if (fluidHandler != null && fluidHandler.drain(1, false) == null) {
                fluidHoldingItems.add(toTry);
            }
        }
    }

    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            IFluidHandlerItem fluidHandlerStack = FluidUtil.getFluidHandler(stack.copy());
            IFluidHandlerItem fluidHandlerTarget = FluidUtil.getFluidHandler(target.copy());

            if (fluidHandlerStack != null && fluidHandlerTarget != null) {
                // check to make sure that both of the stacks can contain fluid
                fluidHandlerStack.drain(Integer.MAX_VALUE, true);
                fluidHandlerTarget.drain(Integer.MAX_VALUE, true);
                ItemStack emptyStack = fluidHandlerStack.getContainer();
                ItemStack emptyTarget = fluidHandlerTarget.getContainer();
                if (StackUtil.isMatchingItem(emptyStack, emptyTarget, true, true)) {
                    return true;
                }
            }
        } else if (type == Type.MATERIAL) {
            FluidStack fStack = FluidUtil.getFluidContained(stack);
            FluidStack fTarget = FluidUtil.getFluidContained(target);
            if (fStack != null && fTarget != null) {
                return fStack.isFluidEqual(fTarget);
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        if (type == Type.TYPE) {
            return FluidUtil.getFluidHandler(stack) != null;
        } else if (type == Type.MATERIAL) {
            return FluidUtil.getFluidContained(stack) != null;
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getClientExamples(Type type, @Nonnull ItemStack stack) {
        if (type == Type.MATERIAL) {
            FluidStack fStack = FluidUtil.getFluidContained(stack);
            if (fStack != null) {
                NonNullList<ItemStack> examples = NonNullList.create();

                for (ItemStack potentialHolder : fluidHoldingItems) {
                    potentialHolder = potentialHolder.copy();
                    IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(potentialHolder);
                    if (fluidHandler != null && (fluidHandler.fill(fStack, true) > 0 || fluidHandler.drain(fStack, false) != null)) {
                        examples.add(fluidHandler.getContainer());
                    }
                }
                return examples;
            }
        } else if (type == Type.TYPE) {
            IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(stack.copy());

            if (fluidHandler != null) {
                NonNullList<ItemStack> examples = NonNullList.create();
                examples.add(stack);
                FluidStack contained = fluidHandler.drain(Integer.MAX_VALUE, true);
                if (contained != null) {
                    examples.add(fluidHandler.getContainer());
                    for (ItemStack potential : fluidHoldingItems) {
                        IFluidHandlerItem potentialHolder = FluidUtil.getFluidHandler(potential);
                        if (potentialHolder.fill(contained, true) > 0) {
                            examples.add(potentialHolder.getContainer());
                        }
                    }
                }
            }
        }
        return null;
    }
}
