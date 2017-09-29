/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;

import buildcraft.transport.BCTransportStatements;

public class PipeBehaviourStripes extends PipeBehaviour implements IStripesActivator, IMjRedstoneReceiver {
    private final MjBattery battery = new MjBattery(256 * MjAPI.MJ);

    @Nullable
    public EnumFacing direction = null;
    private int progress;

    public PipeBehaviourStripes(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourStripes(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
        setDirection(NBTUtilBC.readEnum(nbt.getTag("direction"), EnumFacing.class));
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("battery", battery.serializeNBT());
        nbt.setTag("direction", NBTUtilBC.writeEnum(direction));
        return nbt;
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) {
        super.readPayload(buffer, side, ctx);
        direction = MessageUtil.readEnumOrNull(buffer, EnumFacing.class);
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        MessageUtil.writeEnumOrNull(buffer, direction);
    }

    // Sides

    private void setDirection(@Nullable EnumFacing newValue) {
        if (direction != newValue) {
            direction = newValue;
            if (!pipe.getHolder().getPipeWorld().isRemote) {
                pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            }
        }
    }

    // Actions

    @PipeEventHandler
    public void addInternalActions(PipeEventStatement.AddActionInternal event) {
        for (EnumFacing face : EnumFacing.VALUES) {
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
        for (EnumFacing face : EnumFacing.VALUES) {
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
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourStripes);
    }

    @Override
    public void onTick() {
        World world = pipe.getHolder().getPipeWorld();
        BlockPos pos = pipe.getHolder().getPipePos();
        if (world.isRemote) {
            return;
        }
        if (direction == null || pipe.isConnected(direction)) {
            int sides = 0;
            EnumFacing dir = null;
            for (EnumFacing face : EnumFacing.VALUES) {
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
            BlockPos offset = pos.offset(direction);
            long target = BlockUtil.computeBlockBreakPower(world, offset);
            if (target > 0) {
                int offsetHash = offset.hashCode();
                if (progress < target) {
                    progress += battery.extractPower(0, Math.min(target - progress, MjAPI.MJ * 10));
                    if (progress > 0) {
                        world.sendBlockBreakProgress(offsetHash, offset, (int) (progress * 9 / target));
                    }
                } else {
                    WorldServer server = (WorldServer) world;
                    GameProfile owner = pipe.getHolder().getOwner();
                    FakePlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(server, owner, pos);
                    BreakEvent breakEvent = new BreakEvent(world, offset, world.getBlockState(offset), fakePlayer);
                    MinecraftForge.EVENT_BUS.post(breakEvent);
                    if (!breakEvent.isCanceled()) {
                        NonNullList<ItemStack> dropped = BlockUtil.getItemStackFromBlock(server, offset, owner);
                        if (dropped != null) {
                            for (ItemStack stack : dropped) {
                                sendItem(stack, direction);
                            }
                        }
                        world.sendBlockBreakProgress(offsetHash, offset, -1);
                        world.destroyBlock(offset, false);
                    }
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
        World world = holder.getPipeWorld();
        BlockPos pos = holder.getPipePos();
        FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) world, holder.getOwner(), pos);
        player.inventory.clear();
        // set the main hand of the fake player to the stack
        player.inventory.setInventorySlotContents(player.inventory.currentItem, event.getStack());
        if (PipeApi.stripeRegistry.handleItem(world, pos, direction, event.getStack(), player, this)) {
            event.setStack(StackUtil.EMPTY);
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.removeStackFromSlot(i);
                if (!stack.isEmpty()) {
                    sendItem(stack, direction);
                }
            }
        }
    }

    @Override
    public void dropItem(@Nonnull ItemStack stack, EnumFacing direction) {
        InventoryUtil.drop(pipe.getHolder().getPipeWorld(), pipe.getHolder().getPipePos(), stack);
    }

    @Override
    public boolean sendItem(@Nonnull ItemStack stack, EnumFacing from) {
        PipeFlow flow = pipe.getFlow();
        if (flow instanceof IFlowItems) {
            ((IFlowItems) flow).insertItemsForce(stack, from, null, 0.02);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == MjAPI.CAP_REDSTONE_RECEIVER) {
            return MjAPI.CAP_REDSTONE_RECEIVER.cast(this);
        }
        if (capability == MjAPI.CAP_RECEIVER) {
            return MjAPI.CAP_RECEIVER.cast(this);
        }
        if (capability == MjAPI.CAP_CONNECTOR) {
            return MjAPI.CAP_CONNECTOR.cast(this);
        }
        return super.getCapability(capability, facing);
    }
}
