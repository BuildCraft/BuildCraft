/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.tile;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventTileState;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.PipeEventBus;
import buildcraft.transport.pipe.PluggableHolder;
import buildcraft.transport.plug.FilterEventHandler;
import buildcraft.transport.wire.WireManager;

public class TilePipeHolder extends TileBC_Neptune implements IPipeHolder, ITickable, IDebuggable {

    protected static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("pipe");

    public static final int NET_UPDATE_MULTI = IDS.allocId("UPDATE_MULTI");
    public static final int NET_UPDATE_PIPE_BEHAVIOUR = getReceiverId(PipeMessageReceiver.BEHAVIOUR);
    public static final int NET_UPDATE_PIPE_FLOW = getReceiverId(PipeMessageReceiver.FLOW);
    public static final int NET_UPDATE_PLUG_DOWN = getReceiverId(PipeMessageReceiver.PLUGGABLE_DOWN);
    public static final int NET_UPDATE_PLUG_UP = getReceiverId(PipeMessageReceiver.PLUGGABLE_UP);
    public static final int NET_UPDATE_PLUG_NORTH = getReceiverId(PipeMessageReceiver.PLUGGABLE_NORTH);
    public static final int NET_UPDATE_PLUG_SOUTH = getReceiverId(PipeMessageReceiver.PLUGGABLE_SOUTH);
    public static final int NET_UPDATE_PLUG_WEST = getReceiverId(PipeMessageReceiver.PLUGGABLE_WEST);
    public static final int NET_UPDATE_PLUG_EAST = getReceiverId(PipeMessageReceiver.PLUGGABLE_EAST);
    public static final int NET_UPDATE_WIRES = getReceiverId(PipeMessageReceiver.WIRES);

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private int[] redstoneValues = new int[6];
    private int[] oldRedstoneValues = new int[] { -1, -1, -1, -1, -1, -1 };

    static {
        for (PipeMessageReceiver rec : PipeMessageReceiver.VALUES) {
            IDS.allocId("UPDATE_" + rec);
        }
    }

    public static final int[] NET_UPDATE_PLUGS = { //
        NET_UPDATE_PLUG_DOWN, NET_UPDATE_PLUG_UP, //
        NET_UPDATE_PLUG_NORTH, NET_UPDATE_PLUG_SOUTH, //
        NET_UPDATE_PLUG_WEST, NET_UPDATE_PLUG_EAST,//
    };

    private static int getReceiverId(PipeMessageReceiver type) {
        return NET_UPDATE_MULTI + 1 + type.ordinal();
    }

    public final WireManager wireManager = new WireManager(this);
    public final PipeEventBus eventBus = new PipeEventBus();
    private final Map<EnumFacing, PluggableHolder> pluggables = new EnumMap<>(EnumFacing.class);
    private Pipe pipe;
    private boolean scheduleRenderUpdate = true;
    private final Set<PipeMessageReceiver> networkUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private final Set<PipeMessageReceiver> networkGuiUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private final Map<EnumFacing, WeakReference<TileEntity>> neighbourTiles = new EnumMap<>(EnumFacing.class);
    private NBTTagCompound unknownData;

    public TilePipeHolder() {
        for (EnumFacing side : EnumFacing.VALUES) {
            pluggables.put(side, new PluggableHolder(this, side));
        }
        caps.addCapabilityInstance(PipeApi.CAP_PIPE_HOLDER, this, EnumPipePart.VALUES);
        caps.addCapability(PipeApi.CAP_PIPE, this::getPipe, EnumPipePart.VALUES);
        caps.addCapability(PipeApi.CAP_PLUG, this::getPluggable, EnumPipePart.FACES);
    }

    // Read + write

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (pipe != null) {
            nbt.setTag("pipe", pipe.writeToNbt());
        }
        NBTTagCompound plugs = new NBTTagCompound();
        for (EnumFacing face : EnumFacing.VALUES) {
            NBTTagCompound plugTag = pluggables.get(face).writeToNbt();
            if (!plugTag.hasNoTags()) {
                plugs.setTag(face.getName(), plugTag);
            }
        }
        if (!plugs.hasNoTags()) {
            nbt.setTag("plugs", plugs);
        }
        nbt.setTag("wireManager", wireManager.writeToNbt());
        nbt.setIntArray("redstone", redstoneValues);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("pipe")) {
            try {
                pipe = new Pipe(this, nbt.getCompoundTag("pipe"));
                eventBus.registerHandler(pipe.behaviour);
                eventBus.registerHandler(pipe.flow);
                if (pipe.flow instanceof IFlowItems) {
                    eventBus.registerHandler(FilterEventHandler.class);
                }
            } catch (InvalidInputDataException e) {
                // Unfortunately we can't throw an exception because then this tile won't persist :/
                e.printStackTrace();
                unknownData = nbt.copy();
            }
        }
        NBTTagCompound plugs = nbt.getCompoundTag("plugs");
        for (EnumFacing face : EnumFacing.VALUES) {
            pluggables.get(face).readFromNbt(plugs.getCompoundTag(face.getName()));
        }
        wireManager.readFromNbt(nbt.getCompoundTag("wireManager"));
        if (nbt.hasKey("redstone")) {

            int[] temp = nbt.getIntArray("redstone");
            if (temp.length == 6) {
                redstoneValues = temp;
            }
        }
    }

    // Misc

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        Item item = stack.getItem();
        if (item instanceof IItemPipe) {
            PipeDefinition definition = ((IItemPipe) item).getDefinition();
            this.pipe = new Pipe(this, definition);
            eventBus.registerHandler(pipe.behaviour);
            eventBus.registerHandler(pipe.flow);
            if (pipe.flow instanceof IFlowItems) {
                eventBus.registerHandler(FilterEventHandler.class);
            }
            int meta = stack.getMetadata();
            if (meta > 0 && meta <= 16) {
                pipe.setColour(EnumDyeColor.byMetadata(meta - 1));
            }
        }
        scheduleRenderUpdate();
    }

    public void refreshNeighbours() {
        for (EnumFacing face : EnumFacing.VALUES) {
            WeakReference<TileEntity> current = neighbourTiles.get(face);
            if (current != null) {
                TileEntity tile = current.get();
                if (tile == null || tile.isInvalid()) {
                    neighbourTiles.remove(face);
                } else {
                    continue;
                }
            }
            TileEntity tile = world.getTileEntity(getPos().offset(face));
            if (tile != null) {
                neighbourTiles.put(face, new WeakReference<>(tile));
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        eventBus.fireEvent(new PipeEventTileState.Invalidate(this));
        wireManager.removeParts(new ArrayList<>(wireManager.parts.keySet()));
    }

    @Override
    public void validate() {
        super.validate();
        eventBus.fireEvent(new PipeEventTileState.Validate(this));
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        eventBus.fireEvent(new PipeEventTileState.ChunkUnload(this));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (pipe != null) {
            pipe.onLoad();
        }
    }

    // ITickable

    @Override
    public void update() {
        redstoneValues = new int[6];
        // Tick objects
        if (pipe != null) {
            pipe.onTick();
        }
        for (EnumFacing face : EnumFacing.VALUES) {
            pluggables.get(face).onTick();
        }

        // Send network updates
        if (networkUpdates.size() > 0) {
            // TODO: Multi-update messages! (multiple updates sent in a single message)
            Set<PipeMessageReceiver> parts = EnumSet.copyOf(networkUpdates);
            for (PipeMessageReceiver part : parts) {
                sendNetworkUpdate(getReceiverId(part));
            }
        }
        // No need to send gui updates to specific players if we just sent off messages to all players.
        networkGuiUpdates.removeAll(networkUpdates);
        networkUpdates.clear();

        if (networkGuiUpdates.size() > 0) {
            // TODO: Multi-update messages! (multiple updates sent in a single message)
            Set<PipeMessageReceiver> parts = EnumSet.copyOf(networkGuiUpdates);
            for (PipeMessageReceiver part : parts) {
                sendNetworkGuiUpdate(getReceiverId(part));
            }
        }
        networkGuiUpdates.clear();

        if (scheduleRenderUpdate) {
            scheduleRenderUpdate = false;
            redrawBlock();
        }

        if (!wireManager.inited) {
            wireManager.updateBetweens(false);
            wireManager.inited = true;
        }

        if (!Arrays.equals(redstoneValues, oldRedstoneValues)) {
            Block block = world.getBlockState(pos).getBlock();
            world.notifyNeighborsOfStateChange(pos, block, true);
            for (int i = 0; i < 6; i++) {
                EnumFacing face = EnumFacing.VALUES[i];
                if (oldRedstoneValues[i] != redstoneValues[i]) {
                    world.notifyNeighborsOfStateChange(pos.offset(face), block, true);
                }
            }
            oldRedstoneValues = redstoneValues;
        }
    }

    // Network

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writeCreationPayload(buffer);
                }
                for (EnumFacing face : EnumFacing.VALUES) {
                    pluggables.get(face).writeCreationPayload(buffer);
                }
                wireManager.writePayload(buffer, side);
            } else if (id == NET_UPDATE_PIPE_BEHAVIOUR) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writePayload(buffer, side);
                }
            } else if (id == NET_UPDATE_WIRES) {
                wireManager.writePayload(buffer, side);
            }
        }
        if (id == NET_UPDATE_PIPE_FLOW) {
            if (pipe == null || pipe.flow == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                pipe.flow.writePayload(PipeFlow.NET_ID_UPDATE, buffer, side);
            }
        } else if (id == NET_UPDATE_PLUG_DOWN) pluggables.get(EnumFacing.DOWN).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_UP) pluggables.get(EnumFacing.UP).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_NORTH) pluggables.get(EnumFacing.NORTH).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_SOUTH) pluggables.get(EnumFacing.SOUTH).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_WEST) pluggables.get(EnumFacing.WEST).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_EAST) pluggables.get(EnumFacing.EAST).writePayload(buffer, side);
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                if (buffer.readBoolean()) {
                    pipe = new Pipe(this, buffer, ctx);
                    eventBus.registerHandler(pipe.behaviour);
                    eventBus.registerHandler(pipe.flow);
                    if (pipe.flow instanceof IFlowItems) {
                        eventBus.registerHandler(FilterEventHandler.class);
                    }
                } else if (pipe != null) {
                    eventBus.unregisterHandler(pipe.behaviour);
                    eventBus.unregisterHandler(pipe.flow);
                    pipe = null;
                }
                for (EnumFacing face : EnumFacing.VALUES) {
                    pluggables.get(face).readCreationPayload(buffer);
                }
                wireManager.readPayload(buffer, side, ctx);
            } else if (id == NET_UPDATE_MULTI) {
                int total = buffer.readUnsignedByte();
                for (PipeMessageReceiver type : PipeMessageReceiver.VALUES) {
                    if (((total >> type.ordinal()) & 1) == 1) {
                        readPayload(getReceiverId(type), buffer, side, ctx);
                    }
                }
            } else if (id == NET_UPDATE_PIPE_BEHAVIOUR) {
                if (buffer.readBoolean()) {
                    if (pipe == null) {
                        throw new IllegalStateException("Pipe was null when it shouldn't have been!");
                    } else {
                        pipe.readPayload(buffer, side, ctx);
                    }
                }
            } else if (id == NET_UPDATE_WIRES) {
                wireManager.readPayload(buffer, side, ctx);
            }
        }
        if (id == NET_UPDATE_PIPE_FLOW) {
            if (buffer.readBoolean()) {
                if (pipe == null) {
                    throw new IllegalStateException("Pipe was null when it shouldn't have been!");
                } else {
                    int fId = buffer.readShort();
                    pipe.flow.readPayload(fId, buffer, side);
                }
            }
        } else if (id == NET_UPDATE_PLUG_DOWN) pluggables.get(EnumFacing.DOWN).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_UP) pluggables.get(EnumFacing.UP).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_NORTH) pluggables.get(EnumFacing.NORTH).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_SOUTH) pluggables.get(EnumFacing.SOUTH).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_WEST) pluggables.get(EnumFacing.WEST).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_EAST) pluggables.get(EnumFacing.EAST).readPayload(buffer, side, ctx);
    }

    // IPipeHolder

    @Override
    public World getPipeWorld() {
        return getWorld();
    }

    @Override
    public BlockPos getPipePos() {
        return getPos();
    }

    @Override
    public TileEntity getPipeTile() {
        return this;
    }

    @Override
    public Pipe getPipe() {
        return pipe;
    }

    @Override
    public PipePluggable getPluggable(EnumFacing side) {
        if (side == null) return null;
        return pluggables.get(side).pluggable;
    }

    public PipePluggable replacePluggable(EnumFacing side, PipePluggable with) {
        redstoneValues = new int[6];
        PluggableHolder holder = pluggables.get(side);
        PipePluggable old = holder.pluggable;
        holder.pluggable = with;

        eventBus.unregisterHandler(old);
        eventBus.registerHandler(with);

        if (pipe != null) {
            pipe.markForUpdate();
        }
        if (!world.isRemote && old != with) {
            wireManager.getWireSystems().rebuildWireSystemsAround(this);
        }
        scheduleNetworkUpdate(PipeMessageReceiver.PLUGGABLES[side.getIndex()]);
        scheduleRenderUpdate();
        world.neighborChanged(pos.offset(side), BCTransportBlocks.pipeHolder, pos);
        return old;
    }

    @Override
    public IPipe getNeighbourPipe(EnumFacing side) {
        TileEntity neighbour = getNeighbourTile(side);
        if (neighbour == null) {
            return null;
        }
        return neighbour.getCapability(PipeApi.CAP_PIPE, side.getOpposite());
    }

    @Override
    public <T> T getCapabilityFromPipe(EnumFacing side, @Nonnull Capability<T> capability) {
        PipePluggable plug = getPluggable(side);
        if (plug != null) {
            T t = plug.getInternalCapability(capability);
            if (t != null) {
                return t;
            }
            if (plug.isBlocking()) {
                return null;
            }
        }
        if (pipe.isConnected(side)) {
            TileEntity neighbour = getNeighbourTile(side);
            if (neighbour != null) {
                return neighbour.getCapability(capability, side.getOpposite());
            }
        }
        return null;
    }

    @Override
    public void scheduleRenderUpdate() {
        scheduleRenderUpdate = true;
    }

    @Override
    public void scheduleNetworkUpdate(PipeMessageReceiver... parts) {
        Collections.addAll(networkUpdates, parts);
    }

    @Override
    public void scheduleNetworkGuiUpdate(PipeMessageReceiver... parts) {
        Collections.addAll(networkGuiUpdates, parts);
    }

    @Override
    public void sendMessage(PipeMessageReceiver to, IWriter writer) {
        createAndSendMessage(getReceiverId(to), writer::write);
    }

    @Override
    public void sendGuiMessage(PipeMessageReceiver to, IWriter writer) {
        createAndSendGuiMessage(getReceiverId(to), writer::write);
    }

    @Override
    public WireManager getWireManager() {
        return wireManager;
    }

    @Override
    public boolean fireEvent(PipeEvent event) {
        return eventBus.fireEvent(event);
    }

    @Override
    public int getRedstoneInput(EnumFacing side) {
        if (side == null) {
            return world.isBlockPowered(pos) ? 15 : 0;
        } else {
            return world.getRedstonePower(pos.offset(side), side);
        }
    }

    @Override
    public boolean setRedstoneOutput(EnumFacing side, int value) {
        if (side == null) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                redstoneValues[facing.ordinal()] = value;
            }
        } else {
            redstoneValues[side.ordinal()] = value;
        }
        return true;
    }

    public int getRedstoneOutput(EnumFacing side) {
        return redstoneValues[side.ordinal()];
    }

    // Caps

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (facing != null) {
            PipePluggable plug = getPluggable(facing);
            if (plug != null) {
                T cap = plug.getCapability(capability);
                if (cap != null) return cap;
                if (plug.isBlocking()) return null;
            }
        }
        if (pipe != null) {
            T val = pipe.getCapability(capability, facing);
            if (val != null) {
                return val;
            }
        }
        return super.getCapability(capability, facing);
    }

    // Client side stuffs

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        if (pipe == null) {
            left.add("Pipe = null");
        } else {
            left.add("Pipe:");
            pipe.getDebugInfo(left, right, side);
        }
        left.add("Parts:");
        wireManager.parts
            .forEach((part, color) -> left.add(" - " + part + " = " + color + " = " + wireManager.isPowered(part)));
        left.add("All wire systems in world count = "
            + (world.isRemote ? 0 : wireManager.getWireSystems().wireSystems.size()));
        if (unknownData != null) {
            left.add(unknownData.toString());
        }
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }
}
