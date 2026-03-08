/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.items.FluidItemDrops;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;
import com.google.common.collect.ForwardingList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Provides a simple way to save+load and send+receive data for any number of tanks. This also attempts to fill all of
 * the tanks one by one via the {@link #fill(FluidStack, FluidAction)} and {@link #drain(FluidStack, FluidAction)} methods.*/
public class TankManager extends ForwardingList<Tank> implements IFluidHandlerAdv, INBTSerializable<CompoundTag> {
    private final List<Tank> tanks = new ArrayList<>();

    public TankManager() {
    }

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

    public InteractionResult onActivated(Player player, BlockPos pos, InteractionHand hand) {
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
    public int fill(FluidStack resource, FluidAction doFill) {
        int filled = 0;
        for (Tank tank : getFillOrderTanks()) {
            int used = tank.fill(resource, doFill);
            if (used > 0) {
                resource = resource.copy();
                resource.setAmount(resource.getAmount() - used);
                filled += used;
                if (resource.getAmount() <= 0) {
                    return filled;
                }
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction doDrain) {
        if (resource == null) {
//            return null;
            return StackUtil.EMPTY_FLUID;
        }
        FluidStack draining = new FluidStack(resource, 0);
        int left = resource.getAmount();
        for (Tank tank : getDrainOrderTanks()) {
            // Calen: in 1.18.2 isFluidEqual may ret f if amount of one is 0
            // Calen: should use resource, draining.getFluid() will ret EMPTY
//            if (!draining.isFluidEqual(tank.getFluid()))
            if (tank.getFluid().getRawFluid() != resource.getRawFluid()) {
                continue;
            }
            FluidStack drained = tank.drain(left, doDrain);
//            if (drained != null && drained.getAmount() > 0)
            if (!drained.isEmpty() && drained.getAmount() > 0) {
                draining.setAmount(draining.getAmount() + drained.getAmount());
                left -= drained.getAmount();
            }
        }
//        return draining.getAmount() <= 0 ? null : draining;
        return draining.getAmount() <= 0 ? StackUtil.EMPTY_FLUID : draining;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain) {
        FluidStack draining = null;
        for (Tank tank : getDrainOrderTanks()) {
            if (draining == null) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
//                if (drained != null && drained.getAmount() > 0)
                if (!drained.isEmpty() && drained.getAmount() > 0) {
                    draining = drained;
                    maxDrain -= drained.getAmount();
                }
            } else if (draining.isFluidEqual(tank.getFluid())) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
//                if (drained != null && drained.getAmount() > 0)
                if (!drained.isEmpty() && drained.getAmount() > 0) {
                    draining.setAmount(draining.getAmount() + drained.getAmount());
                    maxDrain -= drained.getAmount();
                }
            }
        }
        return draining;
    }

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, FluidAction doDrain) {
        if (filter == null) {
//            return null;
            return StackUtil.EMPTY_FLUID;
        }
        FluidStack draining = null;
        for (Tank tank : getDrainOrderTanks()) {
            if (!filter.matches(tank.getFluid())) {
                continue;
            }
            if (draining == null) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
//                if (drained != null && drained.getAmount() > 0)
                if (!drained.isEmpty() && drained.getAmount() > 0) {
                    draining = drained;
                    maxDrain -= drained.getAmount();
                }
            } else if (draining.isFluidEqual(tank.getFluid())) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
//                if (drained != null && drained.getAmount() > 0)
                if (!drained.isEmpty() && drained.getAmount() > 0) {
                    draining.setAmount(draining.getAmount() + drained.getAmount());
                    maxDrain -= drained.getAmount();
                }
            }
        }
        return draining == null ? StackUtil.EMPTY_FLUID : draining;
    }

    // Calen: divided into 3 methods...
//    @Override
//    public FluidTankProperties[] getTankProperties() {
//        IFluidTankProperties[] info = new IFluidTankProperties[size()];
//        for (int i = 0; i < size(); i++) {
//            info[i] = get(i).getTankProperties()[0];
//        }
//        return info;
//    }

    @Override
    public int getTanks() {
        return size();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return get(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return get(tank).getCapacity();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        for (Tank t : tanks) {
            nbt.put(t.getTankName(), t.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (Tank t : tanks) {
            t.readFromNBT(nbt.getCompound(t.getTankName()));
        }
    }

    public void writeData(PacketBufferBC buffer) {
        for (Tank tank : tanks) {
            tank.writeToBuffer(buffer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void readData(PacketBufferBC buffer) {
        for (Tank tank : tanks) {
            tank.readFromBuffer(buffer);
        }
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }
}
