/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.tile;

import buildcraft.api.BCModules;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.silicon.plug.FilterEventHandler;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.PipeEventBus;
import buildcraft.transport.pipe.PluggableHolder;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.wire.WireManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

/** {@link IPipeHolder} implemented {@link ITickable} */
public class TilePipeHolder extends TileBC_Neptune implements IPipeHolder, IDebuggable {

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

    private static final ResourceLocation ADVANCEMENT_PLACE_PIPE = new ResourceLocation(
            "buildcrafttransport:pipe_dream"
    );

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
    private final Map<Direction, PluggableHolder> pluggables = new EnumMap<>(Direction.class);
    private Pipe pipe;
    private boolean scheduleRenderUpdate = true;
    private final Set<PipeMessageReceiver> networkUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private final Set<PipeMessageReceiver> networkGuiUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private CompoundTag unknownData;

    public TilePipeHolder(BlockPos pos, BlockState blockState) {
        super(BCTransportBlocks.pipeHolderTile.get(), pos, blockState);

        for (Direction side : Direction.VALUES) {
            pluggables.put(side, new PluggableHolder(this, side));
        }
        caps.addCapabilityInstance(PipeApi.CAP_PIPE_HOLDER, this, EnumPipePart.VALUES);
        caps.addCapability(PipeApi.CAP_PIPE, this::getPipe, EnumPipePart.VALUES);
        caps.addCapability(PipeApi.CAP_PLUG, this::getPluggable, EnumPipePart.FACES);
    }

    // Read + write

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (pipe != null) {
            nbt.put("pipe", pipe.writeToNbt());
        }
        CompoundTag plugs = new CompoundTag();
        for (Direction face : Direction.VALUES) {
            CompoundTag plugTag = pluggables.get(face).writeToNbt();
            if (!plugTag.isEmpty()) {
                plugs.put(face.getName(), plugTag);
            }
        }
        if (!plugs.isEmpty()) {
            nbt.put("plugs", plugs);
        }
        nbt.put("wireManager", wireManager.writeToNbt());
        nbt.putIntArray("redstone", redstoneValues);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("pipe")) {
            try {
                pipe = new Pipe(this, nbt.getCompound("pipe"));
                eventBus.registerHandler(pipe.behaviour);
                eventBus.registerHandler(pipe.flow);
                if (pipe.flow instanceof IFlowItems && BCModules.SILICON.isLoaded()) {
                    eventBus.registerHandler(FilterEventHandler.class);
                }
            } catch (InvalidInputDataException e) {
                // Unfortunately we can't throw an exception because then this tile won't persist :/
                e.printStackTrace();
                unknownData = nbt.copy();
            }
        }
        CompoundTag plugs = nbt.getCompound("plugs");
        for (Direction face : Direction.VALUES) {
            pluggables.get(face).readFromNbt(plugs.getCompound(face.getName()));
        }
        wireManager.readFromNbt(nbt.getCompound("wireManager"));
        if (nbt.contains("redstone")) {

            int[] temp = nbt.getIntArray("redstone");
            if (temp.length == 6) {
                redstoneValues = temp;
            }
        }
    }

    // Misc

    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        Item item = stack.getItem();
        if (item instanceof IItemPipe itemPipe) {
            PipeDefinition definition = itemPipe.getDefinition();
            this.pipe = new Pipe(this, definition);
            eventBus.registerHandler(pipe.behaviour);
            eventBus.registerHandler(pipe.flow);
            if (pipe.flow instanceof IFlowItems && BCModules.SILICON.isLoaded()) {
                eventBus.registerHandler(FilterEventHandler.class);
            }
//            int meta = stack.getMetadata();
//            if (meta > 0 && meta <= 16) {
//                pipe.setColour(EnumDyeColor.byMetadata(meta - 1));
//            }
            pipe.setColour(itemPipe.getColour());
        }
        scheduleRenderUpdate();

        if (!level.isClientSide && hasOwner()) {
            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_PLACE_PIPE);
        }
    }

    // Calen
    private boolean chunkUnloaded = false;

    @Override
//    public void invalidate()
    public void setRemoved() {
//        super.invalidate();
        super.setRemoved();
        eventBus.fireEvent(new PipeEventTileState.Invalidate(this));
//        wireManager.invalidate();
        // Calen: #setRemoved will be called when chunk unload in 1.18.2, but 1.12.2 not
        // wireManager.invalidate() will cause [lib.tile] Ghost-loading tile at ...
        if (!chunkUnloaded) {
            wireManager.invalidate();
        }
    }

    @Override
//    public void validate()
    public void clearRemoved() {
//        super.validate();
        super.clearRemoved();
        eventBus.fireEvent(new PipeEventTileState.Validate(this));
        wireManager.validate();
        // Calen
        chunkUnloaded = false;
    }

    @Override
//    public void onChunkUnload()
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        eventBus.fireEvent(new PipeEventTileState.ChunkUnload(this));
        // Calen
        chunkUnloaded = true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (pipe != null) {
            pipe.onLoad();
        }
        wireManager.validate();
    }

    @Override
    public void onNeighbourBlockChanged(Block block, BlockPos neighbour) {
        super.onNeighbourBlockChanged(block, neighbour);
        if (level.isClientSide) {
            return;
        }
        if (pipe != null) {
            pipe.markForUpdate();
        }
    }

    // ITickable

    @Override
    public void update() {
        IPipeHolder.super.update();
        redstoneValues = new int[6];
        // Tick objects
        if (pipe != null) {
            pipe.onTick();
        }
        for (Direction face : Direction.VALUES) {
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

        wireManager.tick();

        if (!Arrays.equals(redstoneValues, oldRedstoneValues)) {
            Block block = level.getBlockState(worldPosition).getBlock();
//            level.notifyNeighborsOfStateChange(worldPosition, block, true);
            level.updateNeighborsAt(worldPosition, block);
            for (int i = 0; i < 6; i++) {
                Direction face = Direction.VALUES[i];
                if (oldRedstoneValues[i] != redstoneValues[i]) {
//                    level.notifyNeighborsOfStateChange(worldPosition.relative(face), block, true);
                    level.updateNeighborsAt(worldPosition.relative(face), block);
                }
            }
            oldRedstoneValues = redstoneValues;
        }

        /* It's difficult to check to see if we actually have changed at all. So let's just always mark the chunk as
         * dirty instead of making every component do it indervidually. */
        markChunkDirty();
    }

    // Network

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writeCreationPayload(buffer);
                }
                for (Direction face : Direction.VALUES) {
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
        } else if (id == NET_UPDATE_PLUG_DOWN) pluggables.get(Direction.DOWN).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_UP) pluggables.get(Direction.UP).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_NORTH) pluggables.get(Direction.NORTH).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_SOUTH) pluggables.get(Direction.SOUTH).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_WEST) pluggables.get(Direction.WEST).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_EAST) pluggables.get(Direction.EAST).writePayload(buffer, side);
    }

    @Override
//    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                if (buffer.readBoolean()) {
                    pipe = new Pipe(this, buffer, ctx);
                    eventBus.registerHandler(pipe.behaviour);
                    eventBus.registerHandler(pipe.flow);
                    if (pipe.flow instanceof IFlowItems && BCModules.SILICON.isLoaded()) {
                        eventBus.registerHandler(FilterEventHandler.class);
                    }
                } else if (pipe != null) {
                    eventBus.unregisterHandler(pipe.behaviour);
                    eventBus.unregisterHandler(pipe.flow);
                    pipe = null;
                }
                for (Direction face : Direction.VALUES) {
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
        } else if (id == NET_UPDATE_PLUG_DOWN) pluggables.get(Direction.DOWN).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_UP) pluggables.get(Direction.UP).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_NORTH) pluggables.get(Direction.NORTH).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_SOUTH) pluggables.get(Direction.SOUTH).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_WEST) pluggables.get(Direction.WEST).readPayload(buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_EAST) pluggables.get(Direction.EAST).readPayload(buffer, side, ctx);
    }

    // IPipeHolder

    @Override
    public Level getPipeWorld() {
        return getLevel();
    }

    @Override
    public BlockPos getPipePos() {
        return getBlockPos();
    }

    @Override
    public BlockEntity getPipeTile() {
        return this;
    }

    @Override
    public Pipe getPipe() {
        return pipe;
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return canInteractWith(player);
    }

    @Override
    public PipePluggable getPluggable(Direction side) {
        if (side == null) return null;
        return pluggables.get(side).pluggable;
    }

    public PipePluggable replacePluggable(Direction side, PipePluggable with) {
        redstoneValues = new int[6];
        PluggableHolder holder = pluggables.get(side);
        PipePluggable old = holder.pluggable;
        holder.pluggable = with;

        eventBus.unregisterHandler(old);
        eventBus.registerHandler(with);

        if (pipe != null) {
            pipe.markForUpdate();
        }
        if (!level.isClientSide) {
            if (old != with) {
                wireManager.getWireSystems().rebuildWireSystemsAround(this);
            }
            holder.sendNewPluggableData();
        }
        scheduleRenderUpdate();
        level.neighborChanged(getBlockPos().relative(side), BCTransportBlocks.pipeHolder.get(), worldPosition);
        return old;
    }

    @Override
    public IPipe getNeighbourPipe(Direction side) {
        BlockEntity neighbour = getNeighbourTile(side);
        if (neighbour == null) {
            return null;
        }
        return neighbour.getCapability(PipeApi.CAP_PIPE, side.getOpposite()).orElse(null);
    }

    @Override
    public <T> T getCapabilityFromPipe(Direction side, @Nonnull Capability<T> capability) {
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
            BlockEntity neighbour = getNeighbourTile(side);
            if (neighbour != null) {
                return neighbour.getCapability(capability, side.getOpposite()).orElse(null);
            }
        }
        return null;
    }

    @Override
    public void scheduleRenderUpdate() {
        scheduleRenderUpdate = true;
    }

    @Override
    public void scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver... parts) {
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
    public int getRedstoneInput(Direction side) {
        if (side == null) {
//            return level.isBlockIndirectlyGettingPowered(worldPosition);
            return level.getBestNeighborSignal(worldPosition);
        } else {
//            return level.getRedstonePower(worldPosition.relative(side), side);
            return level.getSignal(worldPosition.relative(side), side);
        }
    }

    @Override
    public boolean setRedstoneOutput(Direction side, int value) {
        if (side == null) {
            for (Direction facing : Direction.VALUES) {
                redstoneValues[facing.ordinal()] = value;
            }
        } else {
            redstoneValues[side.ordinal()] = value;
        }
        return true;
    }

    public int getRedstoneOutput(Direction side) {
        return redstoneValues[side.ordinal()];
    }

    // Caps

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (facing != null) {
            PipePluggable plug = getPluggable(facing);
            if (plug != null) {
                LazyOptional<T> cap = plug.getCapability(capability);
//                if (cap != null)
                if (cap.isPresent()) {
                    return cap;
                }
//                if (plug.isBlocking()) return null;
                if (plug.isBlocking()) return LazyOptional.empty();
            }
        }
        if (pipe != null) {
            LazyOptional<T> val = pipe.getCapability(capability, facing);
            if (val.isPresent()) {
                return val;
            }
        }
        return super.getCapability(capability, facing);
    }

    // Client side stuffs

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
        if (pipe == null) {
//            left.add("Pipe = null");
            left.add(Component.literal("Pipe = null"));
        } else {
//            left.add("Pipe:");
            left.add(Component.literal("Pipe:"));
            pipe.getDebugInfo(left, right, side);
        }
//        left.add("Parts:");
        left.add(Component.literal("Parts:"));
        wireManager.parts
//                .forEach((part, color) -> left.add(" - " + part + " = " + color + " = " + wireManager.isPowered(part)));
                .forEach((part, color) -> left.add(Component.literal(" - " + part + " = " + color + " = " + wireManager.isPowered(part))));
//        left.add("All wire systems in world count = " + (level.isClientSide ? 0 : wireManager.getWireSystems().wireSystems.size()));
        left.add(Component.literal("All wire systems in world count = " + (level.isClientSide ? 0 : wireManager.getWireSystems().wireSystems.size())));
        if (unknownData != null) {
//            left.add(unknownData.toString());
            left.add(Component.literal(unknownData.toString()));
        }
    }

//    @Override
//    public boolean hasFastRenderer() {
//        return true;
//    }

    @NotNull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(BlockPipeHolder.PROP_TILE, this).build();
    }

    // MenuProvider

    @NotNull
    @Override
    public Component getDisplayName() {
        ResourceLocation reg = this.getPipe().getDefinition().identifier;
        String tagId = "item.pipe." + reg.getNamespace() + "." + reg.getPath();
        return Component.translatable("item." + TagManager.getTag(tagId, TagManager.EnumTagType.UNLOCALIZED_NAME) + ".name");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        if (this.pipe.behaviour instanceof PipeBehaviourDiamond diamond) {
            return new ContainerDiamondPipe(BCTransportMenuTypes.PIPE_DIAMOND, id, player, diamond);
        } else if (this.pipe.behaviour instanceof PipeBehaviourWoodDiamond woodDiamond) {
            return new ContainerDiamondWoodPipe(BCTransportMenuTypes.PIPE_DIAMOND_WOOD, id, player, woodDiamond);
        } else if (this.pipe.behaviour instanceof PipeBehaviourEmzuli emzuli) {
            return new ContainerEmzuliPipe_BC8(BCTransportMenuTypes.PIPE_EMZULI, id, player, emzuli);
        }
        return null;
    }
}
