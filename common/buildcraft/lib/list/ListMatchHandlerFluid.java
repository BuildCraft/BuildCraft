/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import buildcraft.api.core.BCLog;
import buildcraft.api.lists.ListMatchHandler;

import buildcraft.lib.misc.StackUtil;

public class ListMatchHandlerFluid extends ListMatchHandler {
    private static final List<ItemStack> clientExampleHolders = new ArrayList<>();
    private static boolean isBuilt = false;

    private static void buildClientExampleList() {
        if (isBuilt) {
            return;
        }
        isBuilt = true;
        for (Item item : Item.REGISTRY) {
            NonNullList<ItemStack> stacks = NonNullList.create();
            item.getSubItems(item, CreativeTabs.SEARCH, stacks);
            for (ItemStack toTry : stacks) {
                IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(toTry);
                if (fluidHandler != null && fluidHandler.drain(1, false) == null) {
                    clientExampleHolders.add(toTry);
                }
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
        buildClientExampleList();
        if (type == Type.MATERIAL) {
            FluidStack fStack = FluidUtil.getFluidContained(stack);
            if (fStack != null) {
                NonNullList<ItemStack> examples = NonNullList.create();

                for (ItemStack potentialHolder : clientExampleHolders) {
                    potentialHolder = potentialHolder.copy();
                    IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(potentialHolder);
                    if (fluidHandler != null
                        && (fluidHandler.fill(fStack, true) > 0 || fluidHandler.drain(fStack, false) != null)) {
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
                    for (ItemStack potential : clientExampleHolders) {
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
