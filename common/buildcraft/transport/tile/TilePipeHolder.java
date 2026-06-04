/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport.tile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

// Yarn 1.20.1 renames:
//   ITickable.update()        → BlockEntityTicker pattern (tick() + static ticker())
//   Direction → Direction     DyeColor → DyeColor     NbtCompound → NbtCompound
//   Identifier → Identifier   LivingEntity → LivingEntity   PlayerEntity → PlayerEntity
//   BlockEntity → BlockEntity    world.isClient → world.isClient   getTileEntity → getBlockEntity
//   writeToNBT/readFromNBT(returns) → void writeNbt/readNbt(super-called)
//   invalidate()/validate()    → markRemoved()/cancelRemoval()
//   notifyNeighborsOfStateChange(pos, block, x) → updateNeighborsAlways(pos, block)
//   neighborChanged(pos, block, fromPos)        → updateNeighbor(pos, block, fromPos)
//   isBlockIndirectlyGettingPowered(pos)        → getReceivedRedstonePower(pos)
//   getRedstonePower(pos, side)                 → getEmittedRedstonePower(pos, side)
//   Side → tile-level NetSide (write/readPayload); flow/pipe/pluggable/wire use EnvType (see toEnv)
//   MessageContext → Object (stub)
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventPlaced;
import buildcraft.api.transport.pipe.PipeEventTileState;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.silicon.plug.FilterEventHandler;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.PipeEventBus;
import buildcraft.transport.pipe.PluggableHolder;
import buildcraft.transport.wire.WireManager;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

public class TilePipeHolder extends TileBC_Neptune implements IPipeHolder, IDebuggable, RenderAttachmentBlockEntity {

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
    public static final int NET_CREATE_LANDING_PARTICLE;

    private static final Identifier ADVANCEMENT_PLACE_PIPE = new Identifier(
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
        NET_CREATE_LANDING_PARTICLE = IDS.allocId("CREATE_LANDING_PARTICLE");
    }

    public static final int[] NET_UPDATE_PLUGS = { //
        NET_UPDATE_PLUG_DOWN, NET_UPDATE_PLUG_UP, //
        NET_UPDATE_PLUG_NORTH, NET_UPDATE_PLUG_SOUTH, //
        NET_UPDATE_PLUG_WEST, NET_UPDATE_PLUG_EAST,//
    };

    private static int getReceiverId(PipeMessageReceiver type) {
        return NET_UPDATE_MULTI + 1 + type.ordinal();
    }

    /** Converts the tile-level {@link NetSide} discriminator to the {@link EnvType} expected by the pipe /
     * flow / pluggable / wire payload methods (which were migrated against EnvType, not NetSide). */
    private static EnvType toEnv(NetSide side) {
        return side == NetSide.SERVER ? EnvType.SERVER : EnvType.CLIENT;
    }

    /** STUB(R.Chen): was {@code BCModules.SILICON.isLoaded()} (Forge Loader). BCModules carries heavy Forge
     * deps (FML Loader/LoaderState), so the module-presence check is resolved through the Fabric loader. */
    private static boolean isSiliconLoaded() {
        return FabricLoader.getInstance().isModLoaded("buildcraftsilicon");
    }

    public final WireManager wireManager = new WireManager(this);
    public final PipeEventBus eventBus = new PipeEventBus();
    private final Map<Direction, PluggableHolder> pluggables = new EnumMap<>(Direction.class);
    private Pipe pipe;
    private boolean scheduleRenderUpdate = true;
    private final Set<PipeMessageReceiver> networkUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private final Set<PipeMessageReceiver> networkGuiUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private NbtCompound unknownData;

    // Yarn BlockEntity requires (BlockEntityType<?>, BlockPos, BlockState); the owning BlockPipeHolder passes
    // its registered type. This replaces the Forge no-arg constructor.
    public TilePipeHolder(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        for (Direction side : Direction.values()) {
            pluggables.put(side, new PluggableHolder(this, side));
        }
        // STUB(R.Chen): Forge capability registration replaced by Fabric BlockApiLookup (see
        // registerCapabilities below) — Phase 4F. The Forge form was:
        //   caps.addCapabilityInstance(PipeApi.CAP_PIPE_HOLDER, this, EnumPipePart.VALUES);
        //   caps.addCapability(PipeApi.CAP_PIPE, this::getPipe, EnumPipePart.VALUES);
        //   caps.addCapability(PipeApi.CAP_PLUG, this::getPluggable, EnumPipePart.FACES);
    }

    // BlockEntityTicker — replaces ITickable.update(). Registered against the BlockEntityType in the
    // transport initializer / BlockPipeHolder.getTicker(...) in Phase 4F.
    public static <T extends TilePipeHolder> BlockEntityTicker<T> ticker() {
        return (world, pos, state, tile) -> tile.tick();
    }

    // Fabric BlockApiLookup registration design (Phase 4F)
    // ----------------------------------------------------
    // STUB(R.Chen): the Forge per-side capability provider is replaced by registering SIDED lookups against
    // the registered BlockEntityType once BCTransportBlocks/BCTransportInitializer create it. The design:
    //
    //   ItemStorage.SIDED.registerForBlockEntity((tile, dir) ->
    //       tile.pipe != null ? tile.pipe.flow.getItemStorage(dir) : null, TYPE);
    //   FluidStorage.SIDED.registerForBlockEntity((tile, dir) ->
    //       tile.pipe != null ? tile.pipe.flow.getFluidStorage(dir) : null, TYPE);
    //   EnergyStorage.SIDED.registerForBlockEntity((tile, dir) ->
    //       tile.pipe != null ? tile.pipe.flow.getEnergyStorage(dir) : null, TYPE);
    //
    // Each lambda first consults the pluggable on that side (getCapability/isBlocking), mirroring the old
    // getCapability(Capability, Direction) precedence, then falls through to the pipe + connected neighbour.
    // Left unwired here because the pipe-flow storages and the BlockEntityType are themselves Phase-4F work.
    public static void registerCapabilities() {
        // STUB(R.Chen): wired in Phase 4F once the BlockEntityType + flow storages exist.
    }

    // Read + write

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (pipe != null) {
            nbt.put("pipe", pipe.writeToNbt());
        }
        NbtCompound plugs = new NbtCompound();
        for (Direction face : Direction.values()) {
            NbtCompound plugTag = pluggables.get(face).writeToNbt();
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
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("pipe")) {
            try {
                pipe = new Pipe(this, nbt.getCompound("pipe"));
                eventBus.registerHandler(pipe.behaviour);
                eventBus.registerHandler(pipe.flow);
                if (pipe.flow instanceof IFlowItems && isSiliconLoaded()) {
                    eventBus.registerHandler(FilterEventHandler.class);
                }
            } catch (InvalidInputDataException e) {
                // Unfortunately we can't throw an exception because then this tile won't persist :/
                e.printStackTrace();
                unknownData = nbt.copy();
            }
        }
        NbtCompound plugs = nbt.getCompound("plugs");
        for (Direction face : Direction.values()) {
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
        if (item instanceof IItemPipe) {
            PipeDefinition definition = ((IItemPipe) item).getDefinition();
            this.pipe = new Pipe(this, definition);
            eventBus.registerHandler(pipe.behaviour);
            eventBus.registerHandler(pipe.flow);
            if (pipe.flow instanceof IFlowItems && isSiliconLoaded()) {
                eventBus.registerHandler(FilterEventHandler.class);
            }
            // STUB(R.Chen): item metadata→pipe colour was lost in the 1.13 flattening. Coloured pipe items
            // now carry their colour via the item/NBT instead of stack.getDamage(); restore in Phase 4F.
            //   int meta = stack.getDamage();
            //   if (meta > 0 && meta <= 16) { pipe.setColour(DyeColor.byId(meta - 1)); }
            eventBus.fireEvent(new PipeEventPlaced(this, placer, stack));
        }
        scheduleRenderUpdate();

        if (!world.isClient && hasOwner()) {
            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_PLACE_PIPE);
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        eventBus.fireEvent(new PipeEventTileState.Invalidate(this));
        wireManager.invalidate();
    }

    @Override
    public void cancelRemoval() {
        super.cancelRemoval();
        eventBus.fireEvent(new PipeEventTileState.Validate(this));
        wireManager.validate();
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
        wireManager.validate();
    }

    @Override
    public void onNeighbourBlockChanged(Block block, BlockPos neighbour) {
        super.onNeighbourBlockChanged(block, neighbour);
        if (world.isClient) {
            return;
        }
        if (pipe != null) {
            pipe.markForUpdate();
        }
    }

    // Ticking (was ITickable.update())

    public void tick() {
        redstoneValues = new int[6];
        // Tick objects
        if (pipe != null) {
            pipe.onTick();
        }
        for (Direction face : Direction.values()) {
            pluggables.get(face).onTick();
        }
        if (pipe != null) {
            pipe.postPluggableTick();
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
            Block block = world.getBlockState(pos).getBlock();
            world.updateNeighborsAlways(pos, block);
            for (int i = 0; i < 6; i++) {
                Direction face = Direction.values()[i];
                if (oldRedstoneValues[i] != redstoneValues[i]) {
                    world.updateNeighborsAlways(pos.offset(face), block);
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
    public void writePayload(int id, PacketBufferBC buffer, NetSide side) {
        super.writePayload(id, buffer, side);
        EnvType env = toEnv(side);
        if (side == NetSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writeCreationPayload(buffer);
                }
                for (Direction face : Direction.values()) {
                    pluggables.get(face).writeCreationPayload(buffer);
                }
                wireManager.writePayload(buffer, env);
            } else if (id == NET_UPDATE_PIPE_BEHAVIOUR) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writePayload(buffer, env);
                }
            } else if (id == NET_UPDATE_WIRES) {
                wireManager.writePayload(buffer, env);
            }
        }
        if (id == NET_UPDATE_PIPE_FLOW) {
            if (pipe == null || pipe.flow == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                pipe.flow.writePayload(PipeFlow.NET_ID_UPDATE, buffer, env);
            }
        } else if (id == NET_UPDATE_PLUG_DOWN) pluggables.get(Direction.DOWN).writePayload(buffer, env);
        else if (id == NET_UPDATE_PLUG_UP) pluggables.get(Direction.UP).writePayload(buffer, env);
        else if (id == NET_UPDATE_PLUG_NORTH) pluggables.get(Direction.NORTH).writePayload(buffer, env);
        else if (id == NET_UPDATE_PLUG_SOUTH) pluggables.get(Direction.SOUTH).writePayload(buffer, env);
        else if (id == NET_UPDATE_PLUG_WEST) pluggables.get(Direction.WEST).writePayload(buffer, env);
        else if (id == NET_UPDATE_PLUG_EAST) pluggables.get(Direction.EAST).writePayload(buffer, env);
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, NetSide side, /* STUB(R.Chen): MessageContext */ Object ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        EnvType env = toEnv(side);
        if (side == NetSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                if (buffer.readBoolean()) {
                    pipe = new Pipe(this, buffer, ctx);
                    eventBus.registerHandler(pipe.behaviour);
                    eventBus.registerHandler(pipe.flow);
                    if (pipe.flow instanceof IFlowItems && isSiliconLoaded()) {
                        eventBus.registerHandler(FilterEventHandler.class);
                    }
                } else if (pipe != null) {
                    eventBus.unregisterHandler(pipe.behaviour);
                    eventBus.unregisterHandler(pipe.flow);
                    pipe = null;
                }
                for (Direction face : Direction.values()) {
                    pluggables.get(face).readCreationPayload(buffer);
                }
                wireManager.readPayload(buffer, env, ctx);
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
                        pipe.readPayload(buffer, env, ctx);
                    }
                }
            } else if (id == NET_UPDATE_WIRES) {
                wireManager.readPayload(buffer, env, ctx);
            } else if (id == NET_CREATE_LANDING_PARTICLE) {
                double posX = buffer.readDouble();
                double posY = buffer.readDouble();
                double posZ = buffer.readDouble();
                int number = buffer.readInt();
                BlockPipeHolder.spawnLandingParticles(this, posX, posY, posZ, number);
            }
        }
        if (id == NET_UPDATE_PIPE_FLOW) {
            if (buffer.readBoolean()) {
                if (pipe == null) {
                    throw new IllegalStateException("Pipe was null when it shouldn't have been!");
                } else {
                    int fId = buffer.readShort();
                    pipe.flow.readPayload(fId, buffer, env);
                }
            }
        } else if (id == NET_UPDATE_PLUG_DOWN) pluggables.get(Direction.DOWN).readPayload(buffer, env, ctx);
        else if (id == NET_UPDATE_PLUG_UP) pluggables.get(Direction.UP).readPayload(buffer, env, ctx);
        else if (id == NET_UPDATE_PLUG_NORTH) pluggables.get(Direction.NORTH).readPayload(buffer, env, ctx);
        else if (id == NET_UPDATE_PLUG_SOUTH) pluggables.get(Direction.SOUTH).readPayload(buffer, env, ctx);
        else if (id == NET_UPDATE_PLUG_WEST) pluggables.get(Direction.WEST).readPayload(buffer, env, ctx);
        else if (id == NET_UPDATE_PLUG_EAST) pluggables.get(Direction.EAST).readPayload(buffer, env, ctx);
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
    public BlockEntity getPipeTile() {
        return this;
    }

    @Override
    public Pipe getPipe() {
        return pipe;
    }

    @Override
    public boolean canPlayerInteract(PlayerEntity player) {
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
        if (!world.isClient) {
            if (old != with) {
                wireManager.getWireSystems().rebuildWireSystemsAround(this);
            }
            holder.sendNewPluggableData();
        }
        scheduleRenderUpdate();
        world.updateNeighbor(pos.offset(side), BCTransportBlocks.pipeHolder, pos);
        return old;
    }

    @Override
    public IPipe getNeighbourPipe(Direction side) {
        BlockEntity neighbour = getNeighbourTile(side);
        if (neighbour == null) {
            return null;
        }
        // STUB(R.Chen): was neighbour.getCapability(PipeApi.CAP_PIPE, side.getOpposite()). Until the pipe
        // capability is registered via Fabric BlockApiLookup (Phase 4F), resolve directly through IPipeHolder.
        if (neighbour instanceof IPipeHolder) {
            return ((IPipeHolder) neighbour).getPipe();
        }
        return null;
    }

    @Override
    public <T> T getCapabilityFromPipe(Direction side, @Nonnull /* STUB(R.Chen): Capability<T> */ Object capability) {
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
                // STUB(R.Chen): neighbour.getCapability(capability, side.getOpposite()) → Fabric
                // BlockApiLookup query in Phase 4F.
                return null;
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
    public int getRedstoneInput(Direction side) {
        if (side == null) {
            return world.getReceivedRedstonePower(pos);
        } else {
            return world.getEmittedRedstonePower(pos.offset(side), side);
        }
    }

    @Override
    public boolean setRedstoneOutput(Direction side, int value) {
        if (side == null) {
            for (Direction facing : Direction.values()) {
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
    // STUB(R.Chen): the Forge getCapability(Capability<T>, Direction) override is removed — TileBC_Neptune
    // no longer models capabilities through a getCapability method. The per-side precedence (pluggable →
    // pipe → super) is reproduced by the Fabric BlockApiLookup providers documented in registerCapabilities().

    // Client side stuffs

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
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
            + (world.isClient ? 0 : wireManager.getWireSystems().wireSystems.size()));
        if (unknownData != null) {
            left.add(unknownData.toString());
        }
    }

    // STUB(R.Chen): Forge BlockEntityRenderer "fast renderer" hint had no Yarn equivalent; kept as a plain
    // helper (no @Override) for the Phase 4F render layer to consult.
    @Environment(EnvType.CLIENT)
    public boolean hasFastRenderer() {
        return true;
    }

    // IExtendedBlockState replacement (FRAPI render-attachment)
    // --------------------------------------------------------
    // Forge BlockPipeHolder attached a WeakReference<TilePipeHolder> to the block state via the unlisted
    // PROP_TILE property + getExtendedState(), and the baked model pulled the PipeModelKey from it. 1.20.1 has no
    // IExtendedBlockState; Fabric routes per-tile render data through RenderAttachmentBlockEntity. The attachment
    // is read on the client chunk-mesher thread only (never on a dedicated server), so returning the CLIENT-side
    // PipeModelKey here is safe. The Phase 5 baked pipe model consumes this key.
    @Override
    public Object getRenderAttachmentData() {
        return pipe == null ? null : pipe.getModel();
    }
}
