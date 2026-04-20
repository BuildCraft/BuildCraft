/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ct.buildcraft.lib.tile;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.IPlayerOwned;
import ct.buildcraft.lib.cache.CachedChunk;
import ct.buildcraft.lib.cache.IChunkCache;
import ct.buildcraft.lib.cache.ITileCache;
import ct.buildcraft.lib.cache.TileCacheRet;
import ct.buildcraft.lib.cache.TileCacheType;
import ct.buildcraft.lib.delta.DeltaManager;
import ct.buildcraft.lib.delta.DeltaManager.EnumDeltaMessage;
import ct.buildcraft.lib.fluid.TankManager;
import ct.buildcraft.lib.migrate.BCVersion;
import ct.buildcraft.lib.misc.ChunkUtil;
import ct.buildcraft.lib.misc.FakePlayerProvider;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.misc.PermissionUtil;
import ct.buildcraft.lib.misc.PermissionUtil.PermissionBlock;
import ct.buildcraft.lib.misc.data.IdAllocator;
import ct.buildcraft.lib.net.IPayloadReceiver;
import ct.buildcraft.lib.net.IPayloadWriter;
import ct.buildcraft.lib.net.MessageManager;
import ct.buildcraft.lib.net.MessageUpdateTile;
import ct.buildcraft.lib.tile.item.ItemHandlerManager;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import ct.buildcraft.lib.cap.CapabilityHelper;
import ct.buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import ct.buildcraft.lib.debug.BCAdvDebugging;
import ct.buildcraft.lib.debug.IAdvDebugTarget;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkEvent.Context;

public abstract class TileBC_Neptune extends BlockEntity implements IPayloadReceiver, IAdvDebugTarget, IPlayerOwned {

	public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.tile");

    protected static final IdAllocator IDS = new IdAllocator("tile");

    /** Used for sending all data used for rendering the tile on a client. This does not include items, power, stages,
     * etc (Unless some are shown in the level) */
    public static final int NET_RENDER_DATA = IDS.allocId("RENDER_DATA");
    /** Used for sending all data in the GUI. Basically what has been omitted from {@link #NET_RENDER_DATA} that is
     * shown in the GUI. */
    public static final int NET_GUI_DATA = IDS.allocId("GUI_DATA");
    /** Used for sending the data that would normally be sent with {@link Container#detectAndSendChanges()}. Note that
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
    public final TankManager tankManager = new TankManager();

    /** Handles all of the players that are currently using this tile (have a GUI open) */
    private final Set<Player> usingPlayers = Sets.newIdentityHashSet();
    private GameProfile owner;

    private final IChunkCache chunkCache = new CachedChunk(this);
    private final ITileCache tileCache = TileCacheType.NEIGHBOUR_CACHE.create(this);

    protected final DeltaManager deltaManager = new DeltaManager((gui, type, writer) -> {
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

    public TileBC_Neptune(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
		super(p_155228_, p_155229_, p_155230_);
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

    @Nullable
    public final BlockState getCurrentStateForBlock(Block expectedBlock) {
        BlockState state = getBlockState();
        if (state.getBlock() == expectedBlock) {
            return state;
        }
        return null;
    }

    public final BlockState getNeighbourState(Direction offset) {
        // In the future it is plausible that we might cache block states here.
        // However, until that is implemented, just call the level directly.
        return getOffsetState(offset.getNormal());
    }

    /** @param offset The worldPositionition of the {@link BlockState}, <i>relative</i> to this {@link BlockEntity#getPos()}. */
    public final BlockState getOffsetState(Vec3i offset) {
        return getLocalState(worldPosition.offset(offset));
    }

    /** @param pos The <i>absolute</i> position of the {@link BlockState} . */
    public final BlockState getLocalState(BlockPos pos) {
        if (DEBUG && !level.isLoaded(pos)) {
            BCLog.logger.warn(
                "[lib.tile] Ghost-loading block at " + pos.toShortString() + " (from " + getBlockPos().toShortString() + ")"
            );
        }
        return level.getBlockState(pos);
    }

    public final BlockEntity getNeighbourTile(Direction offset) {
        TileCacheRet cached = tileCache.getTile(offset);
        if (cached != null) {
            return cached.tile;
        }
        if (DEBUG && !level.isLoaded(worldPosition)) {
            BCLog.logger.warn(
                "[lib.tile] Ghost-loading tile at " + (worldPosition).toShortString() + " (from " + getBlockPos().toShortString() + ")"
            );
        }
        return level.getBlockEntity(getBlockPos().offset(offset.getNormal()));
    }

    /** @param offset The worldPositionition of the {@link BlockEntity} to retrieve, <i>relative</i> to this
     *            {@link BlockEntity#getPos()} . */
    public final BlockEntity getOffsetTile(Vec3i offset) {
        return getLocalTile(worldPosition.offset(offset));
    }

    /** @param worldPosition The <i>absolute</i> worldPositionition of the {@link BlockEntity} . */
    public final BlockEntity getLocalTile(BlockPos pos) {
        TileCacheRet cached = tileCache.getTile(pos);
        if (cached != null) {
            return cached.tile;
        }
        if (DEBUG && !level.isLoaded(pos)) {
            BCLog.logger.warn(
                "[lib.tile] Ghost-loading tile at " + pos.toShortString() + " (from " + getBlockPos().toShortString() + ")"
            );
        }
        return level.getBlockEntity(pos);
    }

    public final LevelChunk getContainingChunk() {
        return chunkCache.getChunk(getBlockPos());
    }

    public final LevelChunk getChunk(BlockPos worldPosition) {
        LevelChunk chunk = chunkCache.getChunk(worldPosition);
        if (chunk == null) {
            return ChunkUtil.getChunk(getLevel(), worldPosition, true);
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

    /** Checks to see if this tile can update. The base implementation only checks to see if it has a level. */
    public boolean cannotUpdate() {
        return !hasLevel();
    }

    

    /** Called whenever the block holding this tile is exploded. Called by
     * {@link Block#onBlockExploded(Level, BlockPos, Explosion)} */
    public void onExplode(Explosion explosion) {
    }

    /** Called whenever the block is removed. Called by {@link #onExplode(Explosion)}, and
     * {@link Block#breakBlock(Level, BlockPos, BlockState)} */
    public void onRemove(boolean dropSelf) {
/*        NonNullList<ItemStack> toDrop = NonNullList.create();
        if(dropSelf)
        	toDrop.add(this.getBlockState()
        			.getBlock().getCloneItemStack(getBlockState(), null, level, worldPosition, null));//TODO
        addDrops(toDrop, 0);
        Containers.dropContents(level, worldPosition, toDrop);*/
    }
    

    @Override
    public void setRemoved() {
        super.setRemoved();
        chunkCache.invalidate();
        tileCache.invalidate();
    }

    @Override
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
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        chunkCache.invalidate();
        tileCache.invalidate();
    }
    
    public void update() {
    }

    /** Called whenever {@link #onRemove()} is called (by default). */
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        itemManager.addDrops(toDrop);
        tankManager.addDrops(toDrop);
    }

    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        if (!placer.level.isClientSide()) {
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
        if (owner == null/* || owner == FakePlayerProvider.NULL_PROFILE*/) {
            owner = player.getGameProfile();
            if (owner.getId() == null) {
                // Basically everything relies on the UUID
                throw new IllegalArgumentException("No UUID for owner! ( " + player.getClass() + " " + player + " -> " + owner + " )");
            }
        }
        sendNetworkUpdate(NET_GUI_DATA, player);
        usingPlayers.add(player);
    }

    public void onPlayerClose(Player player) {
        usingPlayers.remove(player);
    }

    public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
		return tankManager.onActivated(player, worldPosition, hand);
    }

    //Only called when neighbor tile changed
    public void onNeighbourBlockChanged(BlockState state, BlockPos neighbor) {
        tileCache.invalidate();
    }
    
    //Called on every neighbor changed
    public void neighbourBlockChanged(BlockState state, BlockPos neighbor, boolean harvest) {
    	
    }

/*    @Override
    public final boolean hasCapability(@Nonnull Capability<?> capability, Direction facing) {
        return getCapability(capability, facing) != null;
    }*/

    @Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap,
			@Nullable Direction side) {
        LazyOptional<T> obj = caps.getCapability(cap, side);
        if (obj.isPresent()) {
            return obj;
        }
        return super.getCapability(cap, side);
	}
    
    
    // Item caps
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before,
        @Nonnull ItemStack after) {
        if (level.isLoaded(worldPosition)) {
            if (getBlockState().hasAnalogOutputSignal()) {
                setChanged();
            } else {
                markChunkDirty();
            }
        }
    }


	/** Cheaper version of {@link #markDirty()} that doesn't update nearby comparators, so all it will do is ensure that
     * the current chunk is saved after the last tick. */
    public void markChunkDirty() {
        if (level != null) {
            level.getChunkAt(worldPosition).setUnsaved(true);
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
            BCLog.logger.warn(msg + getBlockPos());
            owner = FakePlayerProvider.NULL_PROFILE;
        }
        return owner;
    }

    public PermissionUtil.PermissionBlock getPermBlock() {
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
        if (player.blockPosition().distToCenterSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) > 64.0D) {
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
        if (this.hasLevel()) {
            if (level.isClientSide()) {
                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, 0);

                if (DEBUG) {
                    double x = worldPosition.getX() + 0.5;
                    double y = worldPosition.getY() + 0.5;
                    double z = worldPosition.getZ() + 0.5;
                    level.addParticle(ParticleTypes.HEART, x, y, z, 0, 0, 0);
                }
            } else {
                sendNetworkUpdate(NET_REDRAW);
            }
        }
    }

    /** Sends a network update update of the specified ID. */
    public final void sendNetworkUpdate(int id) {
        if (hasLevel()) {
            MessageUpdateTile message = createNetworkUpdate(id);
            if (level.isClientSide()) {
                MessageManager.sendToServer(message);
            } else {
                MessageUtil.sendToAllWatching(level, worldPosition, message);
            }
        }
    }

    public final void sendNetworkGuiTick(Player player) {
        if (hasLevel() && !level.isClientSide()) {
            MessageUpdateTile message = createNetworkUpdate(NET_GUI_TICK);
            if (message.getPayloadSize() <= Short.BYTES) {
                return;
            }
            MessageManager.sendTo(message, (ServerPlayer) player);
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
        if (hasLevel() && target instanceof ServerPlayer) {
            MessageUpdateTile message = createNetworkUpdate(id);
            MessageManager.sendTo(message, (ServerPlayer) target);
        }
    }

    public final MessageUpdateTile createNetworkUpdate(final int id) {
        if (hasLevel()) {
            final LogicalSide side = level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
            return createMessage(id, (buffer) -> writePayload(id, buffer, side));
        } else {
            BCLog.logger.warn("Did not have a level at " + worldPosition + "!");
        }
        return null;
    }

    public final void createAndSendMessage(int id, IPayloadWriter writer) {
        if (hasLevel()) {
            Object message = createMessage(id, writer);
            if (level.isClientSide()) {
                MessageManager.sendToServer(message);
            } else {
                MessageUtil.sendToAllWatching(level, worldPosition, message);
            }
        }
    }

    public final void createAndSendGuiMessage(int id, IPayloadWriter writer) {
        if (hasLevel()) {
            Object message = createMessage(id, writer);
            if (level.isClientSide()) {
                MessageManager.sendToServer(message);
            } else {
                MessageUtil.sendToPlayers(usingPlayers, message);
            }
        }
    }

    public final void createAndSendMessage(int id, ServerPlayer player, IPayloadWriter writer) {
        if (hasLevel()) {
            Object message = createMessage(id, writer);
            MessageManager.sendTo(message, player);
        }
    }

    public final void createAndSendGuiMessage(int id, ServerPlayer player, IPayloadWriter writer) {
        if (usingPlayers.contains(player)) {
            createAndSendMessage(id, player, writer);
        }
    }

    public final MessageUpdateTile createMessage(int id, IPayloadWriter writer) {
    	FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeShort(id);
        writer.write(buffer);
        return new MessageUpdateTile(worldPosition, buffer);
    }



    @Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
    	handleUpdateTag(pkt.getTag());
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
    public CompoundTag getUpdateTag() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(NET_RENDER_DATA);
        writePayload(NET_RENDER_DATA, new FriendlyByteBuf(buf), level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        CompoundTag nbt = super.getUpdateTag();
        nbt.putByteArray("d", bytes);
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
            FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
            readPayload(id, buffer, level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER, null);
            // Make sure that we actually read the entire message rather than just discarding it
            MessageUtil.ensureEmpty(buffer, level.isClientSide, getClass() + ", id = " + getIdAllocator().getNameFor(id));
            spawnReceiveParticles(id);
        } catch (IOException e) {
            throw new RuntimeException("Received an update tag that failed to read correctly!", e);
        }
    }

    private void spawnReceiveParticles(int id) {
        if (DEBUG) {

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
    public final void receivePayload(NetworkEvent.Context ctx, FriendlyByteBuf buffer) throws IOException {
    	try {
        int id = buffer.readUnsignedShort();

        LogicalSide direction = ctx.getDirection().getReceptionSide();
        readPayload(id, buffer, direction ,ctx);

        // Make sure that we actually read the entire message rather than just discarding it
        MessageUtil.ensureEmpty(buffer, level.isClientSide, getClass() + ", id = " + getIdAllocator().getNameFor(id));

        if (direction == LogicalSide.CLIENT) {
            spawnReceiveParticles(id);
        }
    	}
    	catch (Exception e) {
    		BCLog.logger.error(""+this.getClass().getName()+"\n"+e.getMessage());
		}
        return ;
    }

    // ######################
    //
    // Network overridables
    //
    // ######################

    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        // write render data with gui data
        if (id == NET_GUI_DATA) {

            writePayload(NET_RENDER_DATA, buffer, side);

            if (side == LogicalSide.SERVER) {
                MessageUtil.writeGameProfile(buffer, owner);
            }
        }
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                deltaManager.writeDeltaState(false, buffer);
            } else if (id == NET_GUI_DATA) {
                deltaManager.writeDeltaState(true, buffer);
            }
        }
    }

    /** @param ctx The context. Will be null if this is a generic update payload
     * @throws IOException if something went wrong */
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, Context ctx) throws IOException {
        // read render data with gui data
        if (id == NET_GUI_DATA) {
            readPayload(NET_RENDER_DATA, buffer, side, ctx);

            if (side == LogicalSide.CLIENT) {
                owner = MessageUtil.readGameProfile(buffer);
            }
        }
        if (side == LogicalSide.CLIENT) {
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
//        migrateOldNBT(nbt.getInt("data-version"), nbt);
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
    public void saveAdditional(CompoundTag nbt) {
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
        super.saveAdditional(nbt);
    }

	@Override
	public void requestModelDataUpdate() {
//		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
		super.requestModelDataUpdate();
	}
    
    

/*    @Override
    protected void setLevelCreate(Level level) {
        // The default impl doesn't actually set the level for some reason :/
        setLevel(level);
    }*/

    // ##################
    //
    // Advanced debugging
    //
    // ##################

    public boolean isBeingDebuggWed() {
        return BCAdvDebugging.isBeingDebugged(this);
    }

    public void enableDebugging() {
        if (level.isClientSide()) {
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
    public IDetachedRenderer getDebugRenderer() {
        return null;
    }

	public void rotate(Rotation axis) {
	}
}
