/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.IFluidFilter;
import ct.buildcraft.api.core.IFluidHandlerAdv;
import ct.buildcraft.core.BCCoreItems;
import ct.buildcraft.core.item.ItemFragileFluidContainer;
import ct.buildcraft.lib.fluid.Tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;

public class FluidUtilBC {

    public static void pushFluidAround(BlockGetter world, BlockPos pos, Tank tank) {
        FluidStack potential = tank.drain(tank.getFluidAmount(), FluidAction.SIMULATE);
        int drained = 0;
        if (potential == FluidStack.EMPTY || potential.getAmount() <= 0) {
            return;
        }
        FluidStack working = potential.copy();
        for (Direction side : Direction.values()) {
            if (potential.getAmount() <= 0) {
                break;
            }
            BlockEntity target = world.getBlockEntity(pos.offset(side.getNormal()));
            if (target == null) {
                continue;
            }
            IFluidHandler handler = target.getCapability(CapUtil.CAP_FLUIDS, side.getOpposite()).orElse(null);
            if (handler != null) {
                int used = handler.fill(potential.copy(), FluidAction.EXECUTE);

                if (used > 0) {
                    drained += used;
                    potential.setAmount(potential.getAmount() - used);
                }
            }
        }
        if (drained > 0) {
            FluidStack actuallyDrained = tank.drain(drained, FluidAction.EXECUTE);
            if (actuallyDrained == FluidStack.EMPTY || actuallyDrained.getAmount() != drained) {
                String strWorking = StringUtilBC.fluidToString(working);
                String strActual = StringUtilBC.fluidToString(actuallyDrained);
                throw new IllegalStateException("Bad tank! Could drain " + strWorking + " but only drained " + strActual
                    + "( tank " + tank.getClass() + ")");
            }
        }
    }

    public static List<FluidStack> mergeSameFluids(List<FluidStack> fluids) {
        List<FluidStack> stacks = new ArrayList<>();
        fluids.forEach(toAdd -> {
            boolean found = false;
            for (FluidStack stack : stacks) {
                if (stack.isFluidEqual(toAdd)) {
                    stack.setAmount(stack.getAmount() + toAdd.getAmount());
                    found = true;
                }
            }
            if (!found&&!toAdd.isEmpty()) {
                stacks.add(toAdd.copy());
            }
        });
        return stacks;
    }

    public static boolean areFluidStackEqual(FluidStack a, FluidStack b) {
        return (a == FluidStack.EMPTY && b == FluidStack.EMPTY) || (a != FluidStack.EMPTY && a.isFluidEqual(b) && a.getAmount() == b.getAmount());
    }

    public static boolean areFluidsEqual(Fluid a, Fluid b) {
        if (a == Fluids.EMPTY || b == Fluids.EMPTY) {
            return a == b;
        }
        return a.getFluidType() == b.getFluidType();
    }

    /** @return The fluidstack that was moved, or null if no fluid was moved. */
    @Nullable
    public static FluidStack move(IFluidHandler from, IFluidHandler to) {
        return move(from, to, Integer.MAX_VALUE);
    }

    /** @param max The maximum amount of fluid to move.
     * @return The fluidstack that was moved, or null if no fluid was moved. */
    @Nullable
    public static FluidStack move(IFluidHandler from, IFluidHandler to, int max) {
        if (from == null || to == null) {
            return FluidStack.EMPTY;
        }
        FluidStack toDrainPotential;
        if (from instanceof IFluidHandlerAdv) {
            IFluidFilter filter = f -> to.fill(f, FluidAction.SIMULATE) > 0;
            toDrainPotential = ((IFluidHandlerAdv) from).drain(filter, max, FluidAction.SIMULATE);
        } else {
            toDrainPotential = from.drain(max, FluidAction.SIMULATE);
        }
        if (toDrainPotential == FluidStack.EMPTY) {
            return FluidStack.EMPTY;
        }
        int accepted = to.fill(toDrainPotential.copy(), FluidAction.SIMULATE);
        if (accepted <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack toDrain = new FluidStack(toDrainPotential, accepted);
        if (accepted < toDrainPotential.getAmount()) {
            toDrainPotential = from.drain(toDrain, FluidAction.SIMULATE);
            if (toDrainPotential == FluidStack.EMPTY || toDrainPotential.getAmount() < accepted) {
                return FluidStack.EMPTY;
            }
        }
        FluidStack drained = from.drain(toDrain.copy(), FluidAction.EXECUTE);
        if (drained == FluidStack.EMPTY || toDrain.getAmount() != drained.getAmount() || !toDrain.isFluidEqual(drained)) {
            String detail = "(To Drain = " + StringUtilBC.fluidToString(toDrain);
            detail += ",\npotential drain = " + StringUtilBC.fluidToString(toDrainPotential) + ")";
            detail += ",\nactually drained = " + StringUtilBC.fluidToString(drained) + ")";
            detail += ",\nIFluidHandler (from) = " + from.getClass() + "(" + from + ")";
            detail += ",\nIFluidHandler (to) = " + to.getClass() + "(" + to + ")";
            throw new IllegalStateException("Drained fluid did not equal expected fluid!\n" + detail);
        }
        int actuallyAccepted = to.fill(drained, FluidAction.EXECUTE);
        if (actuallyAccepted != accepted) {
            String detail = "(actually accepted = " + actuallyAccepted + ", accepted = " + accepted + ")";
            throw new IllegalStateException("Mismatched IFluidHandler implementations!\n" + detail);
        }
        return new FluidStack(drained, accepted);
    }

    public static boolean onTankActivated(Player player, BlockPos pos, InteractionHand hand,
        IFluidHandler fluidHandler) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return false;
        }
        boolean replace = !player.isCreative();
        boolean single = held.getCount() == 1;
        IFluidHandlerItem flItem;
        if (replace && single) {
            flItem = FluidUtil.getFluidHandler(held).orElse(null);
        } else {
            // replace and not single - need a copy and count set to 1
            // not replace and single - need a copy, does not need change of count but it should be ok
            // not replace and not single - need a copy count set to 1
            ItemStack copy = held.copy();
            copy.setCount(1);
            flItem = FluidUtil.getFluidHandler(copy).orElse(null);
        }
        if (flItem == null) {
            return false;
        }
        Level world = player.level;
        if (world.isClientSide) {
            return true;
        }
        boolean changed = true;
        FluidStack moved;
        if ((moved = FluidUtilBC.move(flItem, fluidHandler)) != FluidStack.EMPTY) {
            SoundUtil.playBucketEmpty(world, pos, moved);
        } else if ((moved = FluidUtilBC.move(fluidHandler, flItem)) != FluidStack.EMPTY) {
            SoundUtil.playBucketFill(world, pos, moved);
        } else {
            changed = false;
        }

        if (changed && replace) {
            if (single) {
                // if it was the single item, replace with changed one
                player.setItemInHand(hand, flItem.getContainer());
            } else {
                // if it was part of stack, shrink stack and give / drop the new one
                held.shrink(1);
                ItemHandlerHelper.giveItemToPlayer(player, flItem.getContainer());
            }
//            player.inventoryContainer.detectAndSendChanges();
        }
        return changed;
    }
    
    public static ItemStack getFragileFluid(FluidStack fluid) {
    //	if(fluid.isEmpty())
 //   		return ItemStack.EMPTY;
    	ItemStack item = new ItemStack(BCCoreItems.FRAGILE_FLUID_SHARD.get());
    	ItemFragileFluidContainer.setFluid(item, fluid);
    	return item;
    }
}
