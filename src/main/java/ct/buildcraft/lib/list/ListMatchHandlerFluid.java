/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.list;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ct.buildcraft.api.lists.ListMatchHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

public class ListMatchHandlerFluid extends ListMatchHandler {
    private static final List<ItemStack> clientExampleHolders = new ArrayList<>();
    private static boolean isBuilt = false;

    private static void buildClientExampleList() {
        if (isBuilt) {
            return;
        }
        isBuilt = true;
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            NonNullList<ItemStack> stacks = NonNullList.create();
            item.fillItemCategory(CreativeModeTab.TAB_SEARCH, stacks);
            for (ItemStack toTry : stacks) {
                IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(toTry).orElse(null);
                if (fluidHandler != null && fluidHandler.drain(1, FluidAction.SIMULATE).isEmpty()) {
                    clientExampleHolders.add(toTry);
                }
            }
        }
    }

    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            IFluidHandlerItem fluidHandlerStack = FluidUtil.getFluidHandler(stack.copy()).orElse(null);
            IFluidHandlerItem fluidHandlerTarget = FluidUtil.getFluidHandler(target.copy()).orElse(null);

            if (fluidHandlerStack != null && fluidHandlerTarget != null) {
                // check to make sure that both of the stacks can contain fluid
                fluidHandlerStack.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
                fluidHandlerTarget.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
                ItemStack emptyStack = fluidHandlerStack.getContainer();
                ItemStack emptyTarget = fluidHandlerTarget.getContainer();
                if (ItemStack.isSameItemSameTags(emptyStack, emptyTarget)) {
                    return true;
                }
            }
        } else if (type == Type.MATERIAL) {
            FluidStack fStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            FluidStack fTarget = FluidUtil.getFluidContained(target).orElse(FluidStack.EMPTY);
            if (!fStack.isEmpty() && !fTarget.isEmpty()) {
                return fStack.isFluidEqual(fTarget);
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        if (type == Type.TYPE) {
            return FluidUtil.getFluidHandler(stack).isPresent();
        } else if (type == Type.MATERIAL) {
            return FluidUtil.getFluidContained(stack).isPresent();
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getClientExamples(Type type, @Nonnull ItemStack stack) {
        buildClientExampleList();
        if (type == Type.MATERIAL) {
            FluidStack fStack = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            if (!fStack.isEmpty()) {
                NonNullList<ItemStack> examples = NonNullList.create();

                for (ItemStack potentialHolder : clientExampleHolders) {
                    potentialHolder = potentialHolder.copy();
                    IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(potentialHolder).orElse(null);
                    if (fluidHandler != null
                        && (fluidHandler.fill(fStack, FluidAction.EXECUTE) > 0 || !fluidHandler.drain(fStack, FluidAction.SIMULATE).isEmpty())) {
                        examples.add(fluidHandler.getContainer());
                    }
                }
                return examples;
            }
        } else if (type == Type.TYPE) {
            IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(stack.copy()).orElse(null);

            if (fluidHandler != null) {
                NonNullList<ItemStack> examples = NonNullList.create();
                examples.add(stack);
                FluidStack contained = fluidHandler.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
                if (contained != null) {
                    examples.add(fluidHandler.getContainer());
                    for (ItemStack potential : clientExampleHolders) {
                        IFluidHandlerItem potentialHolder = FluidUtil.getFluidHandler(potential).orElse(null);
                        if (potentialHolder.fill(contained, FluidAction.EXECUTE) > 0) {
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
