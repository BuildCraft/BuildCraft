/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.api.core.BuildCraftAPI;
import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.IMjRedstoneReceiver;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjBattery;
import ct.buildcraft.api.transport.IStripesActivator;
import ct.buildcraft.api.transport.pipe.IFlowItems;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.api.transport.pipe.PipeEventActionActivate;
import ct.buildcraft.api.transport.pipe.PipeEventHandler;
import ct.buildcraft.api.transport.pipe.PipeEventItem;
import ct.buildcraft.api.transport.pipe.PipeEventStatement;
import ct.buildcraft.api.transport.pipe.PipeFlow;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.InventoryUtil;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.StackUtil;
import ct.buildcraft.transport.BCTransportStatements;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;

public class PipeBehaviourStripes extends PipeBehaviour implements IStripesActivator, IMjRedstoneReceiver {
    private final MjBattery battery = new MjBattery(256 * MjAPI.MJ);

    @Nullable
    public Direction direction = null;
    private int progress;

    public PipeBehaviourStripes(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourStripes(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        battery.deserializeNBT(nbt.getCompound("battery"));
        setDirection(NBTUtilBC.readEnum(nbt.get("direction"), Direction.class));
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("battery", battery.serializeNBT());
        nbt.put("direction", NBTUtilBC.writeEnum(direction));
        return nbt;
    }

    @Override
    public void readPayload(FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        direction = MessageUtil.readEnumOrNull(buffer, Direction.class);
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        MessageUtil.writeEnumOrNull(buffer, direction);
    }

    // Sides

    private void setDirection(@Nullable Direction newValue) {
        if (direction != newValue) {
            direction = newValue;
            if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
                pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            }
        }
    }

    // Actions

    @PipeEventHandler
    public void addInternalActions(PipeEventStatement.AddActionInternal event) {
        for (Direction face : Direction.values()) {
            if (!pipe.isConnected(face)) {
                PipePluggable plug = pipe.getHolder().getPluggable(face);
                if (plug == PipePluggable.EMPTY || !plug.isBlocking()) {
                    event.actions.add(BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]);
                }
            }
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        for (Direction face : Direction.values()) {
            if (event.action == BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]) {
                setDirection(face);
            }
        }
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return battery.getCapacity() - battery.getStored();
    }

    @Override
    public long receivePower(long microJoules, FluidAction simulate) {
        return battery.addPowerChecking(microJoules, simulate);
    }

    // Stripes

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourStripes);
    }

    @Override
    public void onTick() {
        Level world = pipe.getHolder().getPipeWorld();
        if (world.isClientSide()) {
            return;
        }
        BlockPos pos = pipe.getHolder().getPipePos();
        if (direction == null || pipe.isConnected(direction)) {
            int sides = 0;
            Direction dir = null;
            for (Direction face : Direction.values()) {
                if (pipe.isConnected(face)) {
                    sides++;
                    dir = face;
                }
            }
            if (sides == 1) {
                setDirection(dir.getOpposite());
            } else {
                setDirection(null);
            }
        }
        battery.tick(world, pipe.getHolder().getPipePos());
        if (direction != null) {
            BlockPos offset = pos.offset(direction.getNormal());
            long target = BlockUtil.computeBlockBreakPower(world, offset);
            if (target > 0) {
                int offsetHash = offset.hashCode();
                if (progress < target) {
                    progress += battery.extractPower(0, Math.min(target - progress, MjAPI.MJ * 10));
                    if (progress > 0) {
                        world.destroyBlockProgress(offsetHash, offset, (int) (progress * 9 / target));
                        
                    }
                } else {
                	
                    BlockUtil.breakBlockAndGetDrops(
                        (ServerLevel) world,
                        offset,
                        new ItemStack(Items.DIAMOND_PICKAXE),
                        pipe.getHolder().getOwner()
                    ).ifPresent(stacks -> stacks.forEach(stack -> sendItem(stack, direction)));
                    progress = 0;
                }
            }
        } else {
            progress = 0;
        }
    }

    @PipeEventHandler
    public void onDrop(PipeEventItem.Drop event) {
        if (direction == null) {
            return;
        }
        IPipeHolder holder = pipe.getHolder();
        Level world = holder.getPipeWorld();
        BlockPos pos = holder.getPipePos();
        FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerLevel) world, holder.getOwner(), pos);
        player.getInventory().clearContent();;
        // set the main hand of the fake player to the stack
        player.getInventory().setItem(player.getInventory().selected, event.getStack());
        if (PipeApi.stripeRegistry.handleItem(world, pos, direction, event.getStack(), player, this)) {
            event.setStack(StackUtil.EMPTY);
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    sendItem(stack, direction);
                }
            }
        }
    }

    @Override
    public void dropItem(@Nonnull ItemStack stack, Direction direction) {
        InventoryUtil.drop(pipe.getHolder().getPipeWorld(), pipe.getHolder().getPipePos(), stack);
    }

    @Override
    public boolean sendItem(@Nonnull ItemStack stack, Direction from) {
        PipeFlow flow = pipe.getFlow();
        if (flow instanceof IFlowItems) {
            ((IFlowItems) flow).insertItemsForce(stack, from, null, 0.02);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == MjAPI.CAP_REDSTONE_RECEIVER) {
            return LazyOptional.of(() -> this).cast();
        }
        if (capability == MjAPI.CAP_RECEIVER) {
            return LazyOptional.of(() -> this).cast();
        }
        if (capability == MjAPI.CAP_CONNECTOR) {
            return LazyOptional.of(() -> this).cast();
        }
        return super.getCapability(capability, facing);
    }

	@Override
	public void rotate(Rotation rot) {
		if(direction == null || direction.ordinal() <2) {
			return;
		}
		direction = rot.rotate(direction);
	}
    
    
}
