/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.tile;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.IPlayerOwned;
import buildcraft.api.net.IMessage;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.cache.*;
import buildcraft.lib.cap.CapabilityHelper;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.IAdvDebugTarget;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.delta.DeltaManager.EnumDeltaMessage;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.*;
import buildcraft.lib.misc.PermissionUtil.PermissionBlock;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.*;
import buildcraft.lib.tile.item.ItemHandlerManager;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Set;

public abstract class TileBC_Neptune extends BlockEntity implements IPayloadReceiver, IAdvDebugTarget, IPlayerOwned {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.tile");

    protected static final IdAllocator IDS = new IdAllocator("tile");

    /** Used for sending all data used for rendering the tile on a client. This does not include items, power, stages,
     * etc (Unless some are shown in the world) */
    public static final int NET_RENDER_DATA = IDS.allocId("RENDER_DATA");
    /** Used for sending all data in the GUI. Basically what has been omitted from {@link #NET_RENDER_DATA} that is
     * shown in the GUI. */
    public static final int NET_GUI_DATA = IDS.allocId("GUI_DATA");
    /** Used for sending the data that would normally be sent with {@link AbstractContainerMenu#broadcastChanges()}. Note that
     * if no bytes are written then the update message won't be sent. You should detect if any changes have been made to
     * the gui since the last tick, so you don't resend duplicate information if nothing has changed by the next
     * tick. */
    public static final int NET_GUI_TICK = IDS.allocId("GUI_TICK");

    public static final int NET_REN_DELTA_SINGLE = IDS.allocId("REN_DELTA_SINGLE");
    public static final int NET_REN_DELTA_CLEAR = IDS.allocId("REN_DELTA_CLEAR");
    public static final int NET_GUI_DELTA_SINGLE = IDS.allocId("GUI_DELTA_SINGLE");
    public static final int NET_GUI_DELTA_CLEAR = IDS.allocId("GUI_DELTA_CLEAR");

    /** Used for detailed debugging for inspecting every part of the current tile. For example, tanks use this to
     * display which other tanks makeup the whole structure. */
    public static final int NET_ADV_DEBUG = IDS.allocId("DEBUG_DATA");
    public static final int NET_ADV_DEBUG_DISABLE = IDS.allocId("DEBUG_DISABLE");

    /** Used to tell the client to redraw the block. */
    public static final int NET_REDRAW = IDS.allocId("REDRAW");

    protected final CapabilityHelper caps = new CapabilityHelper();
    protected final ItemHandlerManager itemManager = new ItemHandlerManager(this::onSlotChange);
    protected final TankManager tankManager = new TankManager();

    /** Handles all of the players that are currently using this tile (have a GUI open) */
    private final Set<Player> usingPlayers = Sets.newIdentityHashSet();
    // private GameProfile owner;
    protected GameProfile owner;

    private final IChunkCache chunkCache = new CachedChunk(this);
    private final ITileCache tileCache = TileCacheType.NEIGHBOUR_CACHE.create(this);

    protected final DeltaManager deltaManager = new DeltaManager((gui, type, writer) ->
    {
        final int id;
        if (type == EnumDeltaMessage.ADD_SINGLE) {
            id = gui ? NET_GUI_DELTA_SINGLE : NET_REN_DELTA_SINGLE;
        } else if (type == EnumDeltaMessage.SET_VALUE) {
            id = gui ? NET_GUI_DELTA_CLEAR : NET_REN_DELTA_CLEAR;
        } else {
            throw new IllegalArgumentException("Unknown delta message type " + type);
        }
        if (gui) {
            createAndSendGuiMessage(id, writer);
        } else {
            createAndSendMessage(id, writer);
        }
    });

    public TileBC_Neptune(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        caps.addProvider(itemManager);
    }

    // ##################################################
    //
    // Local blockstate + tile entity getters
    //
    // Some of these (may) use a cached version
    // at some point in the future, or are already
    // based on a cache.
    //
    // ##################################################

    public final BlockState getCurrentState() {
        return BlockUtil.getBlockState(level, worldPosition);
    }

    @Nullable
    public final BlockState getCurrentStateForBlock(Block expectedBlock) {
        BlockState state = getCurrentState();
        if (state.getBlock() == expectedBlock) {
            return state;
        }
        return null;
    }

    public final BlockState getNeighbourState(Direction offset) {
        // In the future it is plausible that we might cache block states here.
        // However, until that is implemented, just call the world directly.
        return getOffsetState(offset.getNormal());
    }

    /** @param offset The position of the {@link BlockState}, <i>relative</i> to this {@link BlockEntity#getBlockPos()} . */
    public final BlockState getOffsetState(Vec3i offset) {
        return getLocalState(worldPosition.offset(offset));
    }

    /** @param pos The <i>absolute</i> position of the {@link BlockState} . */
    public final BlockState getLocalState(BlockPos pos) {
        if (DEBUG && !level.isLoaded(pos)) {
            BCLog.logger.warn(
                    "[lib.tile] Ghost-loading block at " + StringUtilBC.blockPosToString(pos) + " (from " + StringUtilBC
                            .blockPosToString(getBlockPos()) + ")"
            );
        }
        return BlockUtil.getBlockState(level, pos, true);
    }

    public final BlockEntity getNeighbourTile(Direction offset) {
        TileCacheRet cached = tileCache.getTile(offset);
        if (cached != null) {
            return cached.tile;
        }
        if (DEBUG && !level.isLoaded(worldPosition)) {
            BCLog.logger.warn(
                    "[lib.tile] Ghost-loading tile at " + StringUtilBC.blockPosToString(worldPosition) + " (from " + StringUtilBC
                            .blockPosToString(getBlockPos()) + ")"
            );
        }
        return BlockUtil.getTileEntity(getLevel(), getBlockPos().relative(offset), true);
    }

    /** @param offset The position of the {@link BlockEntity} to retrieve, <i>relative</i> to this
     *            {@link BlockEntity#getBlockPos()} . */
    public final BlockEntity getOffsetTile(Vec3i offset) {
        return getLocalTile(worldPosition.offset(offset));
    }

    /** @param pos The <i>absolute</i> position of the {@link BlockEntity} . */
    public final BlockEntity getLocalTile(BlockPos pos) {
        TileCacheRet cached = tileCache.getTile(pos);
        if (cached != null) {
            return cached.tile;
        }
        if (DEBUG && !level.isLoaded(pos)) {
            BCLog.logger.warn(
                    "[lib.tile] Ghost-loading tile at " + StringUtilBC.blockPosToString(pos) + " (from " + StringUtilBC
                            .blockPosToString(getBlockPos()) + ")"
            );
        }
        return BlockUtil.getTileEntity(level, pos, true);
    }

    public final LevelChunk getContainingChunk() {
        return chunkCache.getChunk(getBlockPos());
    }

    public final LevelChunk getChunk(BlockPos pos) {
        LevelChunk chunk = chunkCache.getChunk(pos);
        if (chunk == null) {
            return ChunkUtil.getChunk(getLevel(), pos, true);
        }
        return chunk;
    }

    // ##################
    //
    // Misc overridables
    //
    // ##################

    /** @return The {@link IdAllocator} that allocates all ID's for this class, and its parent classes. All subclasses
     *         should override this if they allocate their own ids after calling
     *         {@link IdAllocator#makeChild(String)} */
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    /** Checks to see if this tile can update. The base implementation only checks to see if it has a world. */
    public boolean cannotUpdate() {
        return !hasLevel();
    }

    /** Called in {@link BlockBCTile_Neptune#onRemove(BlockState, Level, BlockPos, BlockState, boolean)}.
     * No longer overrides a super method in 1.18.2 */
    public static boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    /** Called whenever the block holding this tile is exploded. Called by
     * {@link Block#onBlockExploded(BlockState, Level, BlockPos, Explosion)} */
    public void onExplode(Explosion explosion) {

    }

    /** Called whenever the block is removed. Called by {@link #onExplode(Explosion)}, and
     * {@link Block#onRemove(BlockState, Level, BlockPos, BlockState, boolean)} */
    public void onRemove() {
        NonNullList<ItemStack> toDrop = NonNullList.create();
        addDrops(toDrop, 0);
        InventoryUtil.dropAll(level, getBlockPos(), toDrop);
    }

    @Override
//    public void invalidate()
    public void setRemoved() {
        super.setRemoved();
        chunkCache.invalidate();
        tileCache.invalidate();
    }

    @Override
//    public void validate()
    public void clearRemoved() {
        super.clearRemoved();
        chunkCache.invalidate();
        tileCache.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        chunkCache.invalidate();
        tileCache.invalidate();
    }

    @Override
//    public void onChunkUnload()
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        chunkCache.invalidate();
        tileCache.invalidate();
    }

    /** Called whenever {@link #onRemove()} is called (by default). */
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        itemManager.addDrops(toDrop);
        tankManager.addDrops(toDrop);
    }

    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        if (!placer.level().isClientSide) {
            if (placer instanceof Player) {
                Player player = (Player) placer;
                owner = player.getGameProfile();
                if (owner.getId() == null) {
                    // Basically everything relies on the UUID
                    throw new IllegalArgumentException("No UUID for owner! ( " + placer.getClass() + " " + placer + " -> " + owner + " )");
                }
            } else {
                throw new IllegalArgumentException("Not an Player! (placer = " + placer + ")");
            }
        }
    }

    public void onPlayerOpen(Player player) {
        if (owner == null || owner == FakePlayerProvider.NULL_PROFILE) {
            owner = player.getGameProfile();
            if (owner.getId() == null) {
                // Basically everything relies on the UUID
                throw new IllegalArgumentException("No UUID for owner! ( " + player.getClass() + " " + player + " -> " + owner + " )");
            }
        }
        sendNetworkUpdate(NET_GUI_DATA, player);
        usingPlayers.add(player);
    }

    // Calen: from TileBC_Neptune
    // only for Server preparing for opening Gate GUI
    public MessageUpdateTile onServerPlayerOpenNoSend(Player player) {
        if (owner == null || owner == FakePlayerProvider.NULL_PROFILE) {
            owner = player.getGameProfile();
            if (owner.getId() == null) {
                // Basically everything relies on the UUID
                throw new IllegalArgumentException("No UUID for owner! ( " + player.getClass() + " " + player + " -> " + owner + " )");
            }
        }
        usingPlayers.add(player);
        return createNetworkUpdate(NET_GUI_DATA);
    }

    public void onPlayerClose(Player player) {
        usingPlayers.remove(player);
    }

    public InteractionResult onActivated(Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return tankManager.onActivated(player, getBlockPos(), hand);
    }

    public void onNeighbourBlockChanged(Block block, BlockPos nehighbour) {
        tileCache.invalidate();
    }

//    @Override
//    public final boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
//        return getCapability(capability, facing) != null;
//    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        LazyOptional<T> obj = caps.getCapability(capability, facing);
        if (!obj.isPresent()) {
            obj = super.getCapability(capability, facing);
        }
        return obj;
    }

    // Item caps
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        if (level.isLoaded(worldPosition)) {
//            if (getCurrentState().hasComparatorInputOverride())
            if (getCurrentState().hasAnalogOutputSignal()) {
//                markDirty();
                this.setChanged();
            } else {
                markChunkDirty();
            }
        }
    }

    /** Cheaper version of {@link #setChanged()} that doesn't update nearby comparators, so all it will do is ensure that
     * the current chunk is saved after the last tick. */
    public void markChunkDirty() {
        if (level != null) {
//            level.markChunkDirty(this.worldPosition, this);
            level.getChunkAt(this.worldPosition).setUnsaved(true);
        }
    }

    // ##################
    //
    // Permission related
    //
    // ##################

    protected boolean hasOwner() {
        return owner != null;
    }

    @Override
    public GameProfile getOwner() {
        if (owner == null) {
            String msg = "[lib.tile] Unknown owner for " + getClass() + " at ";
            BCLog.logger.warn(msg + StringUtilBC.blockPosToString(getBlockPos()));
            owner = FakePlayerProvider.NULL_PROFILE;
        }
        return owner;
    }

    public PermissionBlock getPermBlock() {
        return new PermissionBlock(this, worldPosition);
    }

    public boolean canEditOther(BlockPos other) {
        return PermissionUtil.hasPermission(
                PermissionUtil.PERM_EDIT, getPermBlock(), PermissionUtil.createFrom(level, other)
        );
    }

    public boolean canPlayerEdit(Player player) {
        return PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, getPermBlock());
    }

    public boolean canInteractWith(Player player) {
        if (level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        if (player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) > 64.0D) {
            return false;
        }
        // edit rather than view because you can normally change the contents from gui interaction
        return canPlayerEdit(player);
    }

    // ##################
    //
    // Network helpers
    //
    // ##################

    /** Tells MC to redraw this block. Note that this sends the NET_REDRAW message. */
    public final void redrawBlock() {
        if (hasLevel()) {
            // Client
            if (level.isClientSide) {
                BlockState state = level.getBlockState(worldPosition);
//                world.notifyBlockUpdate(pos, state, state, 0);
                level.sendBlockUpdated(worldPosition, state, state, 0);

                if (DEBUG) {
                    double x = worldPosition.getX() + 0.5;
                    double y = worldPosition.getY() + 0.5;
                    double z = worldPosition.getZ() + 0.5;
                    level.addParticle(ParticleTypes.HEART, x, y, z, 0, 0, 0);
                }
            }
            // Server
            else {
                sendNetworkUpdate(NET_REDRAW);
            }
        }
    }

    /** Sends a network update update of the specified ID. */
    public final void sendNetworkUpdate(int id) {
        if (hasLevel()) {
            MessageUpdateTile message = createNetworkUpdate(id);
            if (level.isClientSide) {
                MessageManager.sendToServer(message);
            } else {
                MessageUtil.sendToAllWatching(level, worldPosition, message);
            }
        }
    }


    public final void sendNetworkGuiTick(ServerPlayer player) {
        if (hasLevel() && !level.isClientSide) {
            MessageUpdateTile message = createNetworkUpdate(NET_GUI_TICK);
            if (message.getPayloadSize() <= Short.BYTES) {
                return;
            }
            MessageManager.sendTo(message, player);
        }
    }

    public final void sendNetworkGuiUpdate(int id) {
        if (hasLevel()) {
            for (Player player : usingPlayers) {
                sendNetworkUpdate(id, player);
            }
        }
    }

    public final void sendNetworkUpdate(int id, Player target) {
        if (hasLevel() && target instanceof ServerPlayer serverPlayer) {
            MessageUpdateTile message = createNetworkUpdate(id);
            MessageManager.sendTo(message, serverPlayer);
        }
    }

    public final MessageUpdateTile createNetworkUpdate(final int id) {
        if (hasLevel()) {
            final Dist side = level.isClientSide ? Dist.CLIENT : Dist.DEDICATED_SERVER;
            return createMessage(id, (buffer) -> writePayload(id, buffer, side));
        } else {
            BCLog.logger.warn("Did not have a world at " + worldPosition + "!");
        }
        return null;
    }

    public final void createAndSendMessage(int id, IPayloadWriter writer) {
        if (hasLevel()) {
            IMessage message = createMessage(id, writer);
            if (level.isClientSide) {
                MessageManager.sendToServer(message);
            } else {
                MessageUtil.sendToAllWatching(level, worldPosition, message);
            }
        }
    }

    public final void createAndSendGuiMessage(int id, IPayloadWriter writer) {
        if (hasLevel()) {
            IMessage message = createMessage(id, writer);
            if (level.isClientSide) {
                MessageManager.sendToServer(message);
            } else {
                MessageUtil.sendToPlayers(usingPlayers, message);
            }
        }
    }

    public final void createAndSendMessage(int id, ServerPlayer player, IPayloadWriter writer) {
        if (hasLevel()) {
            IMessage message = createMessage(id, writer);
            MessageManager.sendTo(message, player);
        }
    }

    public final void createAndSendGuiMessage(int id, ServerPlayer player, IPayloadWriter writer) {
        if (usingPlayers.contains(player)) {
            createAndSendMessage(id, player, writer);
        }
    }

    public final MessageUpdateTile createMessage(int id, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        buffer.writeShort(id);
        writer.write(buffer);
        return new MessageUpdateTile(worldPosition, buffer);
    }

    @Override
//    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
//    public SPacketUpdateTileEntity getUpdatePacket()
    public Packet<ClientGamePacketListener> getUpdatePacket() {
//        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(NET_RENDER_DATA);
        writePayload(NET_RENDER_DATA, new PacketBufferBC(buf), level.isClientSide ? Dist.CLIENT : Dist.DEDICATED_SERVER);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        CompoundTag nbt = super.getUpdateTag();
        nbt.putByteArray("d", bytes);
        // Calen: in 1.18.2 #load is called when create TE
        // in 1.12.2 readFromNBT will not be called
        this.saveAdditional(nbt);
        return nbt;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        // Explicitly don't read the (server) data from NBT
        super.load(tag);
        if (!tag.contains("d", Tag.TAG_BYTE_ARRAY)) {
            // A bit odd, but ok - this was probably sent by something else
            return;
        }
        byte[] bytes = tag.getByteArray("d");
        if (bytes.length < 2) {
            // less than 2 bytes won't even be enough to read the ID, so we'll treat it as no data.
            BCLog.logger.warn("[lib.tile] Received an update tag that didn't have any data!\n\t(" + tag + ")");
            return;
        }
        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        try {
            int id = buf.readUnsignedShort();
            PacketBufferBC buffer = new PacketBufferBC(buf);
            readPayload(id, buffer, level.isClientSide ? NetworkDirection.PLAY_TO_CLIENT : NetworkDirection.PLAY_TO_SERVER, null);
            // Make sure that we actually read the entire message rather than just discarding it
            MessageUtil.ensureEmpty(buffer, level.isClientSide, getClass() + ", id = " + getIdAllocator().getNameFor(id));
            spawnReceiveParticles(id);
        } catch (IOException e) {
            throw new RuntimeException("Received an update tag that failed to read correctly!", e);
        }
    }

    private void spawnReceiveParticles(int id) {
        if (DEBUG) {
            String name = getIdAllocator().getNameFor(id);

            if (level != null) {
                double x = worldPosition.getX() + 0.5;
                double y = worldPosition.getY() + 0.5;
                double z = worldPosition.getZ() + 0.5;
                double r = 0.01 + (id & 3) / 4.0;
                double g = 0.01 + ((id / 4) & 3) / 4.0;
                double b = 0.01 + ((id / 16) & 3) / 4.0;
                level.addParticle(DustParticleOptions.REDSTONE, x, y, z, r, g, b);
            }
        }
    }

    @Override
    public final IMessage receivePayload(NetworkEvent.Context ctx, PacketBufferBC buffer) throws IOException {
        int id = buffer.readUnsignedShort();
        readPayload(id, buffer, ctx.getDirection(), ctx);

        // Make sure that we actually read the entire message rather than just discarding it
        MessageUtil.ensureEmpty(buffer, level.isClientSide, getClass() + ", id = " + getIdAllocator().getNameFor(id));

        if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            spawnReceiveParticles(id);
        }
        return null;
    }

    // ######################
    //
    // Network overridables
    //
    // ######################

    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        // write render data with gui data
        if (id == NET_GUI_DATA) {

            writePayload(NET_RENDER_DATA, buffer, side);

            if (side == Dist.DEDICATED_SERVER) {
                MessageUtil.writeGameProfile(buffer, owner);
            }
        }
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                deltaManager.writeDeltaState(false, buffer);
            } else if (id == NET_GUI_DATA) {
                deltaManager.writeDeltaState(true, buffer);
            }
        }
    }

    /** @param ctx The context. Will be null if this is a generic update payload
     * @throws IOException if something went wrong */
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        // read render data with gui data
        if (id == NET_GUI_DATA) {
            readPayload(NET_RENDER_DATA, buffer, side, ctx);

            if (side == NetworkDirection.PLAY_TO_CLIENT) {
                owner = MessageUtil.readGameProfile(buffer);
            }
        }
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) deltaManager.receiveDeltaData(false, EnumDeltaMessage.CURRENT_STATE, buffer);
            else if (id == NET_GUI_DATA) deltaManager.receiveDeltaData(true, EnumDeltaMessage.CURRENT_STATE, buffer);
            else if (id == NET_REN_DELTA_SINGLE) deltaManager.receiveDeltaData(
                    false, EnumDeltaMessage.ADD_SINGLE, buffer
            );
            else if (id == NET_GUI_DELTA_SINGLE) deltaManager.receiveDeltaData(
                    true, EnumDeltaMessage.ADD_SINGLE, buffer
            );
            else if (id == NET_REN_DELTA_CLEAR) deltaManager.receiveDeltaData(
                    false, EnumDeltaMessage.SET_VALUE, buffer
            );
            else if (id == NET_GUI_DELTA_CLEAR) deltaManager.receiveDeltaData(true, EnumDeltaMessage.SET_VALUE, buffer);
            else if (id == NET_REDRAW) redrawBlock();
            else if (id == NET_ADV_DEBUG) {
                BCAdvDebugging.setClientDebugTarget(this);
            }
        }
    }

    // ######################
    //
    // NBT handling
    //
    // ######################

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        migrateOldNBT(nbt.getInt("data-version"), nbt);
        deltaManager.readFromNBT(nbt.getCompound("deltas"));
        if (nbt.contains("owner")) {
            owner = NbtUtils.readGameProfile(nbt.getCompound("owner"));
        }
        if (nbt.contains("items", Tag.TAG_COMPOUND)) {
            itemManager.deserializeNBT(nbt.getCompound("items"));
        }
        if (nbt.contains("tanks", Tag.TAG_COMPOUND)) {
            tankManager.deserializeNBT(nbt.getCompound("tanks"));
        }
    }

    protected void migrateOldNBT(int version, CompoundTag nbt) {
        // 7.99.0 -> 7.99.4
        // Most tiles with a single tank saved it under "tank"
        CompoundTag tankComp = nbt.getCompound("tank");
        if (!tankComp.isEmpty()) {
            CompoundTag tanks = new CompoundTag();
            tanks.put("tank", tankComp);
            nbt.put("tanks", tanks);
        }
    }

    @Override
//    public CompoundTag writeToNBT(CompoundTag nbt)
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("data-version", BCVersion.CURRENT.dataVersion);
        nbt.put("deltas", deltaManager.writeToNBT());
        if (owner != null && owner.isComplete() && owner != FakePlayerProvider.NULL_PROFILE) {
            nbt.put("owner", NbtUtils.writeGameProfile(new CompoundTag(), owner));
        }
        CompoundTag items = itemManager.serializeNBT();
        if (!items.isEmpty()) {
            nbt.put("items", items);
        }
        CompoundTag tanks = tankManager.serializeNBT();
        if (!tanks.isEmpty()) {
            nbt.put("tanks", tanks);
        }
    }

//    @Override
//    protected void setWorldCreate(World world) {
//        // The default impl doesn't actually set the world for some reason :/
//        setWorld(world);
//    }

    // ##################
    //
    // Advanced debugging
    //
    // ##################

    public boolean isBeingDebugged() {
        return BCAdvDebugging.isBeingDebugged(this);
    }

    public void enableDebugging() {
        if (level.isClientSide) {
            return;
        }
        BCAdvDebugging.setCurrentDebugTarget(this);
    }

    @Override
    public void disableDebugging() {
        sendNetworkUpdate(NET_ADV_DEBUG_DISABLE);
    }

    @Override
    public boolean doesExistInWorld() {
        return hasLevel() && level.getBlockEntity(worldPosition) == this;
    }

    @Override
    public void sendDebugState() {
        sendNetworkUpdate(NET_ADV_DEBUG);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public DetachedRenderer.IDetachedRenderer getDebugRenderer() {
        return null;
    }
}
