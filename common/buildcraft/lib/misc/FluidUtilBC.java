/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.lib.fluid.Tank;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FluidUtilBC {

    public static void pushFluidAround(IWorld world, BlockPos pos, Tank tank) {
        FluidStack potential = tank.drain(tank.getFluidAmount(), FluidAction.SIMULATE);
        int drained = 0;
        if (potential == null || potential.isEmpty() || potential.getAmount() <= 0) {
            return;
        }
        FluidStack working = potential.copy();
        for (Direction side : Direction.values()) {
            if (potential.getAmount() <= 0) {
                break;
            }
            TileEntity target = world.getBlockEntity(pos.relative(side));
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
//            if (actuallyDrained == null || actuallyDrained.getAmount() != drained)
            if (actuallyDrained == null || actuallyDrained.isEmpty() || actuallyDrained.getAmount() != drained) {
                String strWorking = StringUtilBC.fluidToString(working);
                String strActual = StringUtilBC.fluidToString(actuallyDrained);
                throw new IllegalStateException("Bad tank! Could drain " + strWorking + " but only drained " + strActual
                        + "( tank " + tank.getClass() + ")");
            }
        }
    }

    public static List<FluidStack> mergeSameFluids(List<FluidStack> fluids) {
        List<FluidStack> stacks = new ArrayList<>();
        fluids.forEach(toAdd ->
        {
            boolean found = false;
            for (FluidStack stack : stacks) {
                if (stack.isFluidEqual(toAdd)) {
                    stack.setAmount(stack.getAmount() + toAdd.getAmount());
                    found = true;
                }
            }
            if (!found) {
                stacks.add(toAdd.copy());
            }
        });
        return stacks;
    }

    public static boolean areFluidStackEqual(FluidStack a, FluidStack b) {
        return (a == null && b == null) || (a != null && a.isFluidEqual(b) && a.getAmount() == b.getAmount());
    }

    // Calen: use areFluidsEqualIgnoringStillOrFlow in 1.18.2
    @Deprecated()
    public static boolean areFluidsEqual(Fluid a, Fluid b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getRegistryName().getPath().equals(b.getRegistryName().getPath());
    }

    // Calen
    public static boolean areFluidsEqualIgnoringStillOrFlow(Fluid a, Fluid b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.isSame(b);
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
//            return null;
            return StackUtil.EMPTY_FLUID;
        }
        FluidStack toDrainPotential;
        if (from instanceof IFluidHandlerAdv) {
            IFluidFilter filter = f -> to.fill(f, FluidAction.SIMULATE) > 0;
            toDrainPotential = ((IFluidHandlerAdv) from).drain(filter, max, FluidAction.SIMULATE);
        } else {
            toDrainPotential = from.drain(max, FluidAction.SIMULATE);
        }
//        if (toDrainPotential == null)
        if (toDrainPotential.isEmpty()) {
            return StackUtil.EMPTY_FLUID;
        }
        int accepted = to.fill(toDrainPotential.copy(), FluidAction.SIMULATE);
        if (accepted <= 0) {
//            return null;
            return StackUtil.EMPTY_FLUID;
        }
        FluidStack toDrain = new FluidStack(toDrainPotential, accepted);
        if (accepted < toDrainPotential.getAmount()) {
            toDrainPotential = from.drain(toDrain, FluidAction.SIMULATE);
//            if (toDrainPotential == null || toDrainPotential.getAmount() < accepted)
            if (toDrainPotential.isEmpty() || toDrainPotential.getAmount() < accepted) {
//                return null;
                return StackUtil.EMPTY_FLUID;
            }
        }
        FluidStack drained = from.drain(toDrain.copy(), FluidAction.EXECUTE);
//        if (drained == null || toDrain.getAmount() != drained.getAmount() || !toDrain.isFluidEqual(drained))
        if (drained.isEmpty() || toDrain.getAmount() != drained.getAmount() || !toDrain.isFluidEqual(drained)) {
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

    public static ActionResultType onTankActivated(PlayerEntity player, BlockPos pos, Hand hand, IFluidHandler fluidHandler) {
        World world = player.level;
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return ActionResultType.PASS;
        }
        boolean replace = !player.isCreative();
        boolean single = held.getCount() == 1;
        IFluidHandlerItem flItem = null;
        if (replace && single) {
            flItem = FluidUtil.getFluidHandler(held).resolve().orElse(null);
        } else {
            // replace and not single - need a copy and count set to 1
            // not replace and single - need a copy, does not need change of count but it should be ok
            // not replace and not single - need a copy count set to 1
            ItemStack copy = held.copy();
            copy.setCount(1);
            flItem = FluidUtil.getFluidHandler(copy).resolve().orElse(null);
        }
        if (flItem == null) {
            return ActionResultType.PASS;
        }
        if (world.isClientSide) {
            return ActionResultType.SUCCESS;
        }
        boolean changed = true;
        FluidStack moved;
//        if ((moved = FluidUtilBC.move(flItem, fluidHandler)) != null)
        if (!(moved = FluidUtilBC.move(flItem, fluidHandler)).isEmpty()) {
            SoundUtil.playBucketEmpty(world, pos, moved);
        }
//        else if ((moved = FluidUtilBC.move(fluidHandler, flItem)) != null)
        else if (!(moved = FluidUtilBC.move(fluidHandler, flItem)).isEmpty()) {
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
            player.inventoryMenu.broadcastChanges();
        }
        return ActionResultType.SUCCESS;
    }
}
