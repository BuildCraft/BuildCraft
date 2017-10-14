/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ForwardingList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.items.FluidItemDrops;

import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.net.PacketBufferBC;

/** Provides a simple way to save+load and send+receive data for any number of tanks. This also attempts to fill all of
 * the tanks one by one via the {@link #fill(FluidStack, boolean)} and {@link #drain(FluidStack, boolean)} methods. */
public class TankManager extends ForwardingList<Tank> implements IFluidHandlerAdv, INBTSerializable<NBTTagCompound> {

    private final List<Tank> tanks = new ArrayList<>();

    public TankManager() {}

    public TankManager(Tank... tanks) {
        addAll(Arrays.asList(tanks));
    }

    @Override
    protected List<Tank> delegate() {
        return tanks;
    }

    public void addAll(Tank... values) {
        Collections.addAll(this, values);
    }

    public void addDrops(NonNullList<ItemStack> toDrop) {
        FluidItemDrops.addFluidDrops(toDrop, toArray(new Tank[0]));
    }

    public boolean onActivated(EntityPlayer player, BlockPos pos, EnumHand hand) {
        return FluidUtilBC.onTankActivated(player, pos, hand, this);
    }

    private List<Tank> getFillOrderTanks() {
        List<Tank> list = new ArrayList<>();
        for (Tank t : tanks) {
            if (t.canFill() && !t.canDrain()) {
                list.add(t);
            }
        }
        for (Tank t : tanks) {
            if (t.canFill() && t.canDrain()) {
                list.add(t);
            }
        }
        return list;
    }

    private List<Tank> getDrainOrderTanks() {
        List<Tank> list = new ArrayList<>();
        for (Tank t : tanks) {
            if (!t.canFill() && t.canDrain()) {
                list.add(t);
            }
        }
        for (Tank t : tanks) {
            if (t.canFill() && t.canDrain()) {
                list.add(t);
            }
        }
        return list;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int filled = 0;
        for (Tank tank : getFillOrderTanks()) {
            int used = tank.fill(resource, doFill);
            if (used > 0) {
                resource.amount -= used;
                filled += used;
                if (resource.amount <= 0) {
                    return filled;
                }
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null) {
            return null;
        }
        FluidStack draining = new FluidStack(resource, 0);
        int left = resource.amount;
        for (Tank tank : getDrainOrderTanks()) {
            if (!draining.isFluidEqual(tank.getFluid())) {
                continue;
            }
            FluidStack drained = tank.drain(left, doDrain);
            if (drained != null && drained.amount > 0) {
                draining.amount += drained.amount;
                left -= drained.amount;
            }
        }
        return draining.amount <= 0 ? null : draining;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack draining = null;
        for (Tank tank : getDrainOrderTanks()) {
            if (draining == null) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != null && drained.amount > 0) {
                    draining = drained;
                    maxDrain -= drained.amount;
                }
            } else if (draining.isFluidEqual(tank.getFluid())) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != null && drained.amount > 0) {
                    draining.amount += drained.amount;
                    maxDrain -= drained.amount;
                }
            }
        }
        return draining;
    }

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, boolean doDrain) {
        if (filter == null) {
            return null;
        }
        FluidStack draining = null;
        for (Tank tank : getDrainOrderTanks()) {
            if (!filter.matches(tank.getFluid())) {
                continue;
            }
            if (draining == null) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != null && drained.amount > 0) {
                    draining = drained;
                    maxDrain -= drained.amount;
                }
            } else if (draining.isFluidEqual(tank.getFluid())) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != null && drained.amount > 0) {
                    draining.amount += drained.amount;
                    maxDrain -= drained.amount;
                }
            }
        }
        return draining;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        IFluidTankProperties[] info = new IFluidTankProperties[size()];
        for (int i = 0; i < size(); i++) {
            info[i] = get(i).getTankProperties()[0];
        }
        return info;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        for (Tank t : tanks) {
            nbt.setTag(t.getTankName(), t.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (Tank t : tanks) {
            t.readFromNBT(nbt.getCompoundTag(t.getTankName()));
        }
    }

    public void writeData(PacketBufferBC buffer) {
        for (Tank tank : tanks) {
            tank.writeToBuffer(buffer);
        }
    }

    @SideOnly(Side.CLIENT)
    public void readData(PacketBufferBC buffer) {
        for (Tank tank : tanks) {
            tank.readFromBuffer(buffer);
        }
    }
}
