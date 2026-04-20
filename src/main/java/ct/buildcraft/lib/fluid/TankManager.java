/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ct.buildcraft.lib.fluid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.api.core.IFluidFilter;
import ct.buildcraft.api.core.IFluidHandlerAdv;
import ct.buildcraft.api.items.FluidItemDrops;
import ct.buildcraft.lib.misc.FluidUtilBC;
import com.google.common.collect.ForwardingList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

/** Provides a simple way to save+load and send+receive data for any number of tanks. This also attempts to fill all of
 * the tanks one by one via the {@link #fill(FluidStack, boolean)} and {@link #drain(FluidStack, boolean)} methods. */
public class TankManager extends ForwardingList<Tank> implements IFluidHandlerAdv, INBTSerializable<CompoundTag> {

    private final ArrayList<Tank> tanks = new ArrayList<>();

    public TankManager() {}

    public TankManager(Tank... tanks) {
        addAll(Arrays.asList(tanks));
    }

    @Override
    protected List<Tank> delegate() {
        return tanks;
    }

    public void addAll(Tank... values) {
        Collections.addAll(tanks, values);
        tanks.trimToSize();
    }
    
    public void addLast(Tank value) {
    	tanks.add(value);
    	tanks.trimToSize();
    }

    public void addDrops(NonNullList<ItemStack> toDrop) {
    	if(tanks.isEmpty())
    		return;
        FluidItemDrops.addFluidDrops(toDrop, toArray(new Tank[0]));
    }

    public InteractionResult onActivated(Player player, BlockPos pos, InteractionHand hand) {
//        return FluidUtilBC.onTankActivated(player, pos, hand, this);
    	return FluidUtilBC.onTankActivated(player, pos, hand, this) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    	//return FluidUtil.interactWithFluidHandler(player, hand, this) ? InteractionResult.SUCCESS : InteractionResult.PASS;
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
        if (resource == FluidStack.EMPTY) {
            return resource;
        }
        FluidStack draining = new FluidStack(resource, 0);
        int left = resource.getAmount();
        for (Tank tank : getDrainOrderTanks()) {
            if (!draining.isFluidEqual(tank.getFluid())) {
                continue;
            }
            FluidStack drained = tank.drain(left, doDrain);
            if (drained != null && drained.getAmount() > 0) {
                draining.setAmount(draining.getAmount() + drained.getAmount());
                left -= drained.getAmount();
            }
        }
        return draining.getAmount() <= 0 ? FluidStack.EMPTY : draining;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain) {
        FluidStack draining = FluidStack.EMPTY;
        for (Tank tank : getDrainOrderTanks()) {
            if (draining == FluidStack.EMPTY) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != FluidStack.EMPTY && drained.getAmount() > 0) {
                    draining = drained;
                    maxDrain -= drained.getAmount();
                }
            } else if (draining.isFluidEqual(tank.getFluid())) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != FluidStack.EMPTY && drained.getAmount() > 0) {
                    draining.setAmount(draining.getAmount() + drained.getAmount());
                    maxDrain -= drained.getAmount();
                }
            }
        }
        return draining;
    }

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, FluidAction doDrain) {
        if (filter == FluidStack.EMPTY) {
            return FluidStack.EMPTY;
        }
        FluidStack draining = FluidStack.EMPTY;
        for (Tank tank : getDrainOrderTanks()) {
            if (!filter.matches(tank.getFluid())) {
                continue;
            }
            if (draining == FluidStack.EMPTY) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != FluidStack.EMPTY && drained.getAmount() > 0) {
                    draining = drained;
                    maxDrain -= drained.getAmount();
                }
            } else if (draining.isFluidEqual(tank.getFluid())) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != FluidStack.EMPTY && drained.getAmount() > 0) {
                    draining.setAmount(draining.getAmount() + drained.getAmount());
                    maxDrain -= drained.getAmount();
                }
            }
        }
        return draining;
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

    public void writeData(FriendlyByteBuf buffer) {
        for (Tank tank : tanks) {
            tank.writeToBuffer(buffer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void readData(FriendlyByteBuf buffer) {
        for (Tank tank : tanks) {
            tank.readFromBuffer(buffer);
        }
    }

	@Override
	public int getTanks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public @NotNull FluidStack getFluidInTank(int tank) {
		return null;
	}

	@Override
	public int getTankCapacity(int tank) {
		return tanks.size();
	}

	@Override
	public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean add(Tank e) {
		tanks.add(e);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Tank> c) {
		tanks.addAll(c);
		return false;
	}
}
