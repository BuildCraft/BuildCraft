/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.tile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.mj.ILaserTarget;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.recipes.IngredientStack;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.api.tiles.TilesAPI;
import ct.buildcraft.lib.misc.LocaleUtil;
import ct.buildcraft.lib.misc.data.AverageLong;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public abstract class TileLaserTableBase extends TileBC_Neptune implements ILaserTarget, IDebuggable {
    private static final long MJ_FLOW_ROUND = MjAPI.MJ / 10;
    private final AverageLong avgPower = new AverageLong(120);
    public long avgPowerClient;
    public long power;

    protected TileLaserTableBase(BlockEntityType<? extends TileLaserTableBase> type, BlockPos pos, BlockState state) {
    	super(type, pos, state);
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, () -> getTarget() > 0, EnumPipePart.VALUES);
    }

    public abstract long getTarget();

    @Override
    public long getRequiredLaserPower() {
        return getTarget() - power;
    }

    @Override
    public long receiveLaserPower(long microJoules) {
        long received = Math.min(microJoules, getRequiredLaserPower());
        power += received;
        avgPower.push(received);
        return microJoules - received;
    }

    @Override
    public boolean isInvalidTarget() {
        return isRemoved();
    }

    @Override
    public void update() {
        avgPower.tick();
        if (level.isClientSide) {
            return;
        }

        if (getTarget() <= 0) {
            power = 0;
            avgPower.clear();
        }
    }
    
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
        nbt.putLong("power", power);
	}
	
    @Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		power = nbt.getLong("power");
	}


    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_GUI_TICK) {
                buffer.writeLong(power);
                double avg = avgPower.getAverage();
                long pwrAvg = Math.round(avg);
                long div = pwrAvg / MJ_FLOW_ROUND;
                long mod = pwrAvg % MJ_FLOW_ROUND;
                int mj = (int) (div) + ((mod > MJ_FLOW_ROUND / 2) ? 1 : 0);
                buffer.writeInt(mj);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_GUI_TICK) {
                power = buffer.readLong();
                avgPowerClient = buffer.readInt() * MJ_FLOW_ROUND;
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("power - " + LocaleUtil.localizeMj(power));
        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
    }

    protected boolean extract(ItemHandlerSimple inv, Collection<IngredientStack> items, boolean simulate,
        boolean precise) {
        AtomicLong remainingStacks = new AtomicLong(inv.stacks.stream().filter(stack -> !stack.isEmpty()).count());
        boolean allItemsConsumed = items.stream().allMatch((definition) -> {
            int remaining = definition.count;
            for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
                ItemStack slotStack = inv.getStackInSlot(i);
                if (slotStack.isEmpty()) continue;
                if (definition.ingredient.test(slotStack)) {
                    int spend = Math.min(remaining, slotStack.getCount());
                    remaining -= spend;
                    if (!simulate) {
                        slotStack.setCount(slotStack.getCount() - spend);
                        inv.setStackInSlot(i, slotStack);
                    }
                }
            }
            if (remaining == 0) {
                remainingStacks.decrementAndGet();
                return true;
            }
            return false;
        });
        return allItemsConsumed && (!precise || remainingStacks.get() == 0);
    }
}
