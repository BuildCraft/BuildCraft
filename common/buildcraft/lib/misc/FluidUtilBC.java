/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.lib.fluid.Tank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FluidUtilBC {
    public static void pushFluidAround(IBlockAccess world, BlockPos pos, Tank tank) {
        FluidStack potential = tank.drain(tank.getFluidAmount(), false);
        int drained = 0;
        if (potential == null || potential.amount <= 0) {
            return;
        }
        FluidStack working = potential.copy();
        for (EnumFacing side : EnumFacing.VALUES) {
            if (potential.amount <= 0) {
                break;
            }
            TileEntity target = world.getTileEntity(pos.offset(side));
            if (target == null) {
                continue;
            }
            IFluidHandler handler = target.getCapability(CapUtil.CAP_FLUIDS, side.getOpposite());
            if (handler != null) {
                int used = handler.fill(potential, true);

                if (used > 0) {
                    drained += used;
                    potential.amount -= used;
                }
            }
        }
        if (drained > 0) {
            FluidStack actuallyDrained = tank.drain(drained, true);
            if (actuallyDrained == null || actuallyDrained.amount != drained) {
                throw new IllegalStateException("Bad tank! Could drain " + working + " but only drained "
                    + actuallyDrained + "( tank " + tank.getClass() + ")");
            }
        }
    }

    public static List<FluidStack> mergeSameFluids(List<FluidStack> fluids) {
        List<FluidStack> stacks = new ArrayList<>();
        fluids.forEach(toAdd -> {
            boolean found = false;
            for (FluidStack stack : stacks) {
                if (stack.isFluidEqual(toAdd)) {
                    stack.amount += toAdd.amount;
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
        return (a == null && b == null) || (a != null && a.isFluidEqual(b) && a.amount == b.amount);
    }

    public static boolean areFluidsEqual(Fluid a, Fluid b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getName().equals(b.getName());
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
            return null;
        }
        final FluidStack toDrainPotential;
        if (from instanceof IFluidHandlerAdv) {
            IFluidFilter filter = f -> to.fill(f, false) > 0;
            toDrainPotential = ((IFluidHandlerAdv) from).drain(filter, max, false);
        } else {
            toDrainPotential = from.drain(max, false);
        }
        int accepted = to.fill(toDrainPotential, false);
        if (accepted <= 0) {
            return null;
        }
        if (toDrainPotential == null) throw new NullPointerException();
        FluidStack toDrain = new FluidStack(toDrainPotential, accepted);
        FluidStack drained = from.drain(toDrain, true);
        if (drained != null) {
            if (!toDrain.isFluidEqual(drained) || toDrain.amount != drained.amount) {
                String detail = "(To Drain = " + StringUtilBC.fluidToString(toDrain);
                detail += ", actually drained = " + StringUtilBC.fluidToString(drained) + ")";
                throw new IllegalStateException("Drained fluid did not equal expected fluid!\n" + detail);
            }
            int actuallyAccepted = to.fill(drained, true);
            if (actuallyAccepted != accepted) {
                String detail = "(actually accepted = " + actuallyAccepted + ", accepted = " + accepted + ")";
                throw new IllegalStateException("Mismatched IFluidHandler implementations!\n" + detail);
            }
            return new FluidStack(drained, accepted);
        }
        return null;
    }

    public static boolean onTankActivated(EntityPlayer player, BlockPos pos, EnumHand hand,
        IFluidHandler fluidHandler) {
        ItemStack held = player.getHeldItem(hand);
        if (held == null) {
            return false;
        }
        boolean replace = !player.capabilities.isCreativeMode;
        boolean single = held.stackSize == 1;
        IFluidHandler flItem;
        ItemStack container;
        if (replace && single) {
            flItem = FluidUtil.getFluidHandler(held);
            container = held;
        } else {
            // replace and not single - need a copy and count set to 1
            // not replace and single - need a copy, does not need change of count but it should be ok
            // not replace and not single - need a copy count set to 1
            ItemStack copy = held.copy();
            copy.stackSize = 1;
            flItem = FluidUtil.getFluidHandler(copy);
            container = copy;
        }
        if (flItem == null) {
            return false;
        }
        World world = player.world;
        if (world.isRemote) {
            return true;
        }
        boolean changed = true;
        FluidStack moved;
        if ((moved = FluidUtilBC.move(flItem, fluidHandler)) != null) {
            SoundUtil.playBucketEmpty(world, pos, moved);
        } else if ((moved = FluidUtilBC.move(fluidHandler, flItem)) != null) {
            SoundUtil.playBucketFill(world, pos, moved);
        } else {
            changed = false;
        }

        if (changed && replace) {
            if (single) {
                // if it was the single item, replace with changed one
                player.setHeldItem(hand, container);
            } else {
                // if it was part of stack, shrink stack and give / drop the new one
                held.stackSize -= 1;
                ItemHandlerHelper.giveItemToPlayer(player, container);
            }
            player.inventoryContainer.detectAndSendChanges();
        }
        return true;
    }
}
