/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.misc.*;
import buildcraft.transport.BCTransportStatements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

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
//    public void readPayload(PacketBuffer buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(FriendlyByteBuf buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        direction = MessageUtil.readEnumOrNull(buffer, Direction.class);
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, Dist side) {
        super.writePayload(buffer, side);
        MessageUtil.writeEnumOrNull(buffer, direction);
    }

    // Sides

    private void setDirection(@Nullable Direction newValue) {
        if (direction != newValue) {
            direction = newValue;
//            if (!pipe.getHolder().getPipeWorld().isRemote) {
//                pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
//            }
            // Calen: when world loading, NPE: Cannot read field "isClientSide" because the return value of "buildcraft.api.transport.pipe.IPipeHolder.getPipeWorld()" is null
            pipe.getHolder().runWhenWorldNotNull(
                    () ->
                    {
                        if (!pipe.getHolder().getPipeWorld().isClientSide) {
                            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
                        }
                    },
                    false
            );
        }
    }

    // Actions

    @PipeEventHandler
    public void addInternalActions(PipeEventStatement.AddActionInternal event) {
        for (Direction face : Direction.VALUES) {
            if (!pipe.isConnected(face)) {
                PipePluggable plug = pipe.getHolder().getPluggable(face);
                if (plug == null || !plug.isBlocking()) {
                    event.actions.add(BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]);
                }
            }
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        for (Direction face : Direction.VALUES) {
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
    public long receivePower(long microJoules, boolean simulate) {
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
        BlockPos pos = pipe.getHolder().getPipePos();
        if (world.isClientSide) {
            return;
        }
        if (direction == null || pipe.isConnected(direction)) {
            int sides = 0;
            Direction dir = null;
            for (Direction face : Direction.VALUES) {
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
            BlockPos offset = pos.relative(direction);
            long target = BlockUtil.computeBlockBreakPower(world, offset);
            if (target > 0) {
                int offsetHash = offset.hashCode();
                if (progress < target) {
                    progress += battery.extractPower(0, Math.min(target - progress, MjAPI.MJ * 10));
                    if (progress > 0) {
//                        world.sendBlockBreakProgress(offsetHash, offset, (int) (progress * 9 / target));
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
        // Calen
        if (!(world instanceof ServerLevel)) {
            return;
        }
        BlockPos pos = holder.getPipePos();
        FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerLevel) world, holder.getOwner(), pos);
//        player.inventory.clear();
        player.getInventory().clearContent();
        // set the main hand of the fake player to the stack
//        player.inventory.setInventorySlotContents(player.inventory.currentItem, event.getStack());
        player.getInventory().setItem(player.getInventory().selected, event.getStack());
        if (PipeApi.stripeRegistry.handleItem(world, pos, direction, event.getStack(), player, this)) {
            event.setStack(StackUtil.EMPTY);
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().removeItem(i, player.getInventory().getItem(i).getCount());
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == MjAPI.CAP_REDSTONE_RECEIVER) {
//            return MjAPI.CAP_REDSTONE_RECEIVER.cast(this);
            return LazyOptional.of(() -> this).cast();
        }
        if (capability == MjAPI.CAP_RECEIVER) {
//            return MjAPI.CAP_RECEIVER.cast(this);
            return LazyOptional.of(() -> this).cast();
        }
        if (capability == MjAPI.CAP_CONNECTOR) {
//            return MjAPI.CAP_CONNECTOR.cast(this);
            return LazyOptional.of(() -> this).cast();
        }
//        return super.getCapability(capability, facing);
        return super.getCapability(capability, facing);
    }
}
