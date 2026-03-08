/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ListMatchHandlerFluid extends ListMatchHandler {
    private static final List<ItemStack> clientExampleHolders = new ArrayList<>();
    private static boolean isBuilt = false;

    private static void buildClientExampleList() {
        if (isBuilt) {
            return;
        }
        isBuilt = true;
//        for (Item item : Item.REGISTRY)
        for (Item item : ForgeRegistries.ITEMS) {
            NonNullList<ItemStack> stacks = NonNullList.create();
//            item.getSubItems(CreativeModeTab.TAB_SEARCH, stacks);
            item.fillItemCategory(CreativeModeTab.TAB_SEARCH, stacks);
            for (ItemStack toTry : stacks) {
//                IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(toTry);
                IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(toTry).orElse(null);
//                if (fluidHandler != null && fluidHandler.drain(1, false) == null)
                if (fluidHandler != null && !fluidHandler.drain(1, IFluidHandler.FluidAction.SIMULATE).isEmpty()) {
                    clientExampleHolders.add(toTry);
                }
            }
        }
    }

    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
//            IFluidHandlerItem fluidHandlerStack = FluidUtil.getFluidHandler(stack.copy());
            IFluidHandlerItem fluidHandlerStack = FluidUtil.getFluidHandler(stack.copy()).orElse(null);
//            IFluidHandlerItem fluidHandlerTarget = FluidUtil.getFluidHandler(target.copy());
            IFluidHandlerItem fluidHandlerTarget = FluidUtil.getFluidHandler(target.copy()).orElse(null);

            if (fluidHandlerStack != null && fluidHandlerTarget != null) {
                // check to make sure that both of the stacks can contain fluid
//                fluidHandlerStack.drain(Integer.MAX_VALUE, true);
                fluidHandlerStack.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
//                fluidHandlerTarget.drain(Integer.MAX_VALUE, true);
                fluidHandlerTarget.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                ItemStack emptyStack = fluidHandlerStack.getContainer();
                ItemStack emptyTarget = fluidHandlerTarget.getContainer();
                if (StackUtil.isMatchingItem(emptyStack, emptyTarget, true, true)) {
                    return true;
                }
            }
        } else if (type == Type.MATERIAL) {
//            FluidStack fStack = FluidUtil.getFluidContained(stack);
            FluidStack fStack = FluidUtil.getFluidContained(stack).orElse(StackUtil.EMPTY_FLUID);
//            FluidStack fTarget = FluidUtil.getFluidContained(target);
            FluidStack fTarget = FluidUtil.getFluidContained(target).orElse(StackUtil.EMPTY_FLUID);
//            if (fStack != null && fTarget != null)
            if (!fStack.isEmpty() && !fTarget.isEmpty()) {
                return fStack.isFluidEqual(fTarget);
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        if (type == Type.TYPE) {
//            return FluidUtil.getFluidHandler(stack) != null;
            return FluidUtil.getFluidHandler(stack).isPresent();
        } else if (type == Type.MATERIAL) {
//            return FluidUtil.getFluidContained(stack) != null;
            return FluidUtil.getFluidContained(stack).isPresent();
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getClientExamples(Type type, @Nonnull ItemStack stack) {
        buildClientExampleList();
        if (type == Type.MATERIAL) {
//            FluidStack fStack = FluidUtil.getFluidContained(stack);
            FluidStack fStack = FluidUtil.getFluidContained(stack).orElse(StackUtil.EMPTY_FLUID);
//            if (fStack != null)
            if (!fStack.isEmpty()) {
                NonNullList<ItemStack> examples = NonNullList.create();

                for (ItemStack potentialHolder : clientExampleHolders) {
                    potentialHolder = potentialHolder.copy();
//                    IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(potentialHolder);
                    IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(potentialHolder).orElse(null);
                    if (fluidHandler != null
//                            && (fluidHandler.fill(fStack, true) > 0 || fluidHandler.drain(fStack, false) != null)
                            && (fluidHandler.fill(fStack, FluidAction.EXECUTE) > 0 || !fluidHandler.drain(fStack, FluidAction.SIMULATE).isEmpty())
                    )
                    {
                        examples.add(fluidHandler.getContainer());
                    }
                }
                return examples;
            }
        } else if (type == Type.TYPE) {
//            IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(stack.copy());
            IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(stack.copy()).orElse(null);

            if (fluidHandler != null) {
                NonNullList<ItemStack> examples = NonNullList.create();
                examples.add(stack);
//                FluidStack contained = fluidHandler.drain(Integer.MAX_VALUE, true);
                FluidStack contained = fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                if (!contained.isEmpty()) {
                    examples.add(fluidHandler.getContainer());
                    for (ItemStack potential : clientExampleHolders) {
//                        IFluidHandlerItem potentialHolder = FluidUtil.getFluidHandler(potential);
                        IFluidHandlerItem potentialHolder = FluidUtil.getFluidHandler(potential).orElse(null);
//                        if (potentialHolder.fill(contained, true) > 0)
                        if (potentialHolder != null && potentialHolder.fill(contained, IFluidHandler.FluidAction.EXECUTE) > 0) {
                            examples.add(potentialHolder.getContainer());
                        }
                    }
                }
                return examples;
            }
        }
        return null;
    }
}
