/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileTank extends TileBC_Neptune implements ITickable, IDebuggable, IFluidHandlerAdv {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("tank");
    public static final int NET_FLUID_DELTA = IDS.allocId("FLUID_DELTA");

    private static boolean isPlayerInteracting = false;

    public final Tank tank = new Tank("tank", 16000, this) {

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            dirty = true;
        }
    };
    public final FluidSmoother smoothedTank = new FluidSmoother(w -> createAndSendMessage(NET_FLUID_DELTA, w), tank);

    private boolean dirty = false;

    public TileTank() {
        tankManager.add(tank);// FIXME: SAVING IS ALL SORTS OF BUGGED
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, this, EnumPipePart.VALUES);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    // ITickable

    @Override
    public void update() {
        smoothedTank.tick(getWorld());

        if (!world.isRemote && dirty) {
            markDirty();
            dirty = false;
        }
    }

    // TileEntity

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (!placer.world.isRemote) {
            List<TileTank> tanks = getTanks();
            FluidStack fluid = null;
            for (TileTank tile : tanks) {
                FluidStack held = tile.tank.getFluid();
                if (held == null) {
                    continue;
                }
                if (fluid == null) {
                    fluid = held;
                } else if (!fluid.isFluidEqual(held)) {
                    return;
                }
            }
            if (fluid == null) {
                return;
            }
            if (fluid.getFluid().isGaseous(fluid)) {
                Collections.reverse(tanks);
            }
            TileTank prev = null;
            isPlayerInteracting = true;
            for (TileTank tile : tanks) {
                if (prev != null) {
                    FluidUtilBC.move(tile.tank, prev.tank);
                }
                prev = tile;
            }
            isPlayerInteracting = false;
        }
    }

    public boolean onActivate(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty()) {
            return false;
        }
        boolean replace = !player.capabilities.isCreativeMode;
        IFluidHandlerItem flItem = FluidUtil.getFluidHandler(replace ? held : held.copy());
        if (flItem == null) {
            return false;
        }
        if (getWorld().isRemote) {
            return true;
        }
        isPlayerInteracting = true;
        boolean changed = true;
        FluidStack moved;
        if ((moved = FluidUtilBC.move(flItem, this)) != null) {
            SoundUtil.playBucketEmpty(getWorld(), getPos(), moved);
        } else if ((moved = FluidUtilBC.move(this, flItem)) != null) {
            SoundUtil.playBucketFill(getWorld(), getPos(), moved);
        } else {
            changed = false;
        }
        if (changed & replace) {
            player.setHeldItem(hand, flItem.getContainer());
            player.inventoryContainer.detectAndSendChanges();
        }
        isPlayerInteracting = false;
        return true;
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_FLUID_DELTA, buffer, side);
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.writeInit(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_FLUID_DELTA, buffer, side, ctx);
                smoothedTank.resetSmoothing(getWorld());
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.handleMessage(getWorld(), buffer);
            }
        }
    }

    // IDebuggable

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("fluid = " + tank.getDebugString());
        smoothedTank.getDebugInfo(left, right, side);
    }

    // Rendering

    @SideOnly(Side.CLIENT)
    public FluidStackInterp getFluidForRender(float partialTicks) {
        return smoothedTank.getFluidForRender(partialTicks);
    }

    // Tank helper methods

    private TileTank getTank(BlockPos at) {
        TileEntity tile = world.getTileEntity(at);
        if (tile instanceof TileTank) {
            return (TileTank) tile;
        }
        return null;
    }

    private List<TileTank> getTanks() {
        List<TileTank> tanks = new ArrayList<>();
        BlockPos currentPos = pos;
        while (true) {
            TileTank tankUp = getTank(currentPos);
            if (tankUp != null) {
                tanks.add(tankUp);
            } else {
                break;
            }
            currentPos = currentPos.up();
        }
        currentPos = pos.down();
        while (true) {
            TileTank tankBelow = getTank(currentPos);
            if (tankBelow != null) {
                tanks.add(0, tankBelow);
            } else {
                break;
            }
            currentPos = currentPos.down();
        }
        return tanks;
    }

    // IFluidHandler

    @Override
    public IFluidTankProperties[] getTankProperties() {
        List<TileTank> tanks = getTanks();
        TileTank bottom = tanks.get(0);
        FluidStack total = bottom.tank.getFluid();
        int capacity = 0;
        if (total == null) {
            for (TileTank t : tanks) {
                capacity += t.tank.getCapacity();
            }
        } else {
            total = total.copy();
            total.amount = 0;
            for (TileTank t : tanks) {
                FluidStack other = t.tank.getFluid();
                if (other != null) {
                    total.amount += other.amount;
                }
                capacity += t.tank.getCapacity();
            }
        }
        return new IFluidTankProperties[] { new FluidTankProperties(total, capacity) };
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        int filled = 0;
        List<TileTank> tanks = getTanks();
        for (TileTank t : tanks) {
            FluidStack current = t.tank.getFluid();
            if (current != null && !current.isFluidEqual(resource)) {
                return 0;
            }
        }
        boolean gas = resource.getFluid().isGaseous(resource);
        if (gas) {
            Collections.reverse(tanks);
        }
        for (TileTank t : tanks) {
            int tankFilled = t.tank.fill(resource, doFill);
            if (tankFilled > 0) {
                if (isPlayerInteracting & doFill) {
                    t.sendNetworkUpdate(NET_RENDER_DATA);
                }
                resource.amount -= tankFilled;
                filled += tankFilled;
                if (resource.amount == 0) {
                    break;
                }
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return drain((fluid) -> true, maxDrain, doDrain);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null) {
            return null;
        }
        return drain(resource::isFluidEqual, resource.amount, doDrain);
    }

    // IFluidHandlerAdv

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        }
        List<TileTank> tanks = getTanks();
        boolean gas = false;
        for (TileTank tile : tanks) {
            FluidStack fluid = tile.tank.getFluid();
            if (fluid != null) {
                gas = fluid.getFluid().isGaseous(fluid);
                break;
            }
        }
        if (!gas) {
            Collections.reverse(tanks);
        }
        FluidStack total = null;
        for (TileTank t : tanks) {
            int realMax = maxDrain - (total == null ? 0 : total.amount);
            if (realMax <= 0) {
                break;
            }
            FluidStack drained = t.tank.drain(filter, realMax, doDrain);
            if (drained == null) continue;
            if (isPlayerInteracting & doDrain) {
                t.sendNetworkUpdate(NET_RENDER_DATA);
            }
            if (total == null) {
                total = drained.copy();
                total.amount = 0;
            }
            total.amount += drained.amount;
        }
        return total;
    }
}
