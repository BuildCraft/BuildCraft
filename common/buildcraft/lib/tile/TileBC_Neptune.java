/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.tile;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.permission.IPlayerOwned;

import buildcraft.lib.cap.CapabilityHelper;
import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;
import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.IAdvDebugTarget;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.delta.DeltaManager.EnumDeltaMessage;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.PermissionUtil;
import buildcraft.lib.misc.PermissionUtil.PermissionBlock;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.IPayloadReceiver;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.EnumTagTypeMulti;
import buildcraft.lib.tile.item.ItemHandlerManager;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class TileBC_Neptune extends TileEntity implements IPayloadReceiver, IAdvDebugTarget, IPlayerOwned {
    public static final boolean DEBUG_PARTICLES = BCDebugging.shouldDebugLog("tile.debug.particles");

    protected static final IdAllocator IDS = new IdAllocator("tile");

    /** Used for sending all data used for rendering the tile on a client. This does not include items, power, stages,
     * etc (Unless some are shown in the world) */
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
    protected final ItemHandlerManager itemManager = caps.addProvider(new ItemHandlerManager(this::onSlotChange));

    /** Handles all of the players that are currently using this tile (have a GUI open) */
    private final Set<EntityPlayer> usingPlayers = Sets.newIdentityHashSet();
    private GameProfile owner;

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
            this.createAndSendGuiMessage(id, writer);
        } else {
            this.createAndSendMessage(id, writer);
        }
    });

    public TileBC_Neptune() {
        caps.addProvider(itemManager);
    }

    public static <T extends TileBC_Neptune> void registerTile(Class<T> tileClass, String id) {
        String regName = TagManager.getTag(id, EnumTagType.REGISTRY_NAME);
        String[] alternatives = TagManager.getMultiTag(id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
        GameRegistry.registerTileEntityWithAlternatives(tileClass, regName, alternatives);
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

    public final IBlockState getCurrentState() {
        return BlockUtil.getBlockState(getWorld(), getPos());
    }

    @Nullable
    public final IBlockState getCurrentStateForBlock(Block expectedBlock) {
        IBlockState state = getCurrentState();
        if (state.getBlock() == expectedBlock) {
            return state;
        }
        return null;
    }

    public final IBlockState getNeighbourState(EnumFacing offset) {
        return getOffsetState(offset.getDirectionVec());
    }

    /** @param offset The position of the {@link IBlockState}, <i>relative</i> to this {@link TileEntity#getPos()} . */
    public final IBlockState getOffsetState(Vec3i offset) {
        return getLocalState(getPos().add(offset));
    }

    /** @param pos The <i>absolute</i> position of the {@link IBlockState} . */
    public final IBlockState getLocalState(BlockPos pos) {
        return BlockUtil.getBlockState(getWorld(), pos, isInThisChunk(pos));
    }

    public final TileEntity getNeighbourTile(EnumFacing offset) {
        return getOffsetTile(offset.getDirectionVec());
    }

    /** @param offset The position of the {@link TileEntity} to retrieve, <i>relative</i> to this
     *            {@link TileEntity#getPos()} . */
    public final TileEntity getOffsetTile(Vec3i offset) {
        return getLocalTile(getPos().add(offset));
    }

    /** @param pos The <i>absolute</i> position of the {@link TileEntity} . */
    public final TileEntity getLocalTile(BlockPos pos) {
        return BlockUtil.getTileEntity(getWorld(), pos, isInThisChunk(pos));
    }

    private boolean isInThisChunk(BlockPos other) {
        return pos.getX() / 16 == other.getX() / 16//
            && pos.getZ() / 16 == other.getZ() / 16;
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
        return !hasWorld();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    /** Called whenever the block holding this tile is exploded. Called by
     * {@link Block#onBlockExploded(World, BlockPos, Explosion)} */
    public void onExplode(Explosion explosion) {
        onRemove();
    }

    /** Called whenever the block is removed. Called by {@link #onExplode(Explosion)}, and
     * {@link Block#breakBlock(World, BlockPos, IBlockState)} */
    public void onRemove() {}

    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        if (!placer.world.isRemote) {
            if (placer instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) placer;
                this.owner = player.getGameProfile();
                if (!owner.isComplete()) {
                    throw new IllegalArgumentException("Incomplete owner! ( " + placer + " -> " + owner + " )");
                }
            }
        }
    }

    public void onPlayerOpen(EntityPlayer player) {
        if (owner == null) {
            owner = player.getGameProfile();
            if (!owner.isComplete()) {
                owner = null;
            }
        }
        sendNetworkUpdate(NET_GUI_DATA, player);
        usingPlayers.add(player);
    }

    public void onPlayerClose(EntityPlayer player) {
        usingPlayers.remove(player);
    }

    @Override
    public final boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return caps.getCapability(capability, facing);
    }

    // Item caps
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        if (world.isBlockLoaded(getPos())) {
            markDirty();
        }
    }

    // ##################
    //
    // Permission related
    //
    // ##################

    @Override
    public GameProfile getOwner() {
        return owner;
    }

    public PermissionUtil.PermissionBlock getPermBlock() {
        return new PermissionBlock(this, getPos());
    }

    public boolean canEditOther(BlockPos other) {
        return PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, getPermBlock(), PermissionUtil.createFrom(getWorld(), other));
    }

    public boolean canPlayerEdit(EntityPlayer player) {
        return PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, getPermBlock());
    }

    public boolean canInteractWith(EntityPlayer player) {
        if (world.getTileEntity(getPos()) != this) {
            return false;
        }
        if (player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) > 64.0D) {
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
        if (hasWorld()) {
            if (world.isRemote) {
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 0);

                if (DEBUG_PARTICLES) {
                    double x = getPos().getX() + 0.5;
                    double y = getPos().getY() + 0.5;
                    double z = getPos().getZ() + 0.5;
                    world.spawnParticle(EnumParticleTypes.HEART, x, y, z, 0, 0, 0);
                }
            } else {
                sendNetworkUpdate(NET_REDRAW);
            }
        }
    }

    /** Sends a network update update of the specified ID. */
    public final void sendNetworkUpdate(int id) {
        if (hasWorld()) {
            MessageUpdateTile message = createNetworkUpdate(id);
            MessageUtil.sendToAllWatching(this.world, this.getPos(), message);
        }
    }

    public final void sendNetworkGuiTick(EntityPlayer player) {
        if (hasWorld() && !world.isRemote) {
            MessageUpdateTile message = createNetworkUpdate(NET_GUI_TICK);
            if (message.getPayloadSize() <= Short.BYTES) {
                return;
            }
            MessageUtil.getWrapper().sendTo(message, (EntityPlayerMP) player);
        }
    }

    public final void sendNetworkGuiUpdate(int id) {
        if (hasWorld()) {
            for (EntityPlayer player : usingPlayers) {
                sendNetworkUpdate(id, player);
            }
        }
    }

    public final void sendNetworkUpdate(int id, EntityPlayer target) {
        if (hasWorld() && target instanceof EntityPlayerMP) {
            MessageUpdateTile message = createNetworkUpdate(id);
            MessageUtil.getWrapper().sendTo(message, (EntityPlayerMP) target);
        }
    }

    public final MessageUpdateTile createNetworkUpdate(final int id) {
        if (hasWorld()) {
            final Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
            return createMessage(id, (buffer) -> writePayload(id, buffer, side));
        } else {
            BCLog.logger.warn("Did not have a world at " + getPos() + "!");
        }
        return null;
    }

    public final void createAndSendMessage(int id, IPayloadWriter writer) {
        if (hasWorld()) {
            IMessage message = createMessage(id, writer);
            MessageUtil.sendToAllWatching(this.world, this.getPos(), message);
        }
    }

    public final void createAndSendGuiMessage(int id, IPayloadWriter writer) {
        if (hasWorld()) {
            IMessage message = createMessage(id, writer);
            MessageUtil.sendToPlayers(usingPlayers, message);
        }
    }

    public final MessageUpdateTile createMessage(int id, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        buffer.writeShort(id);
        writer.write(buffer);
        return new MessageUpdateTile(getPos(), buffer);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        IBlockState state = getWorld().getBlockState(getPos());
        int meta = state.getBlock().getMetaFromState(state);
        return new SPacketUpdateTileEntity(getPos(), meta, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeShort(NET_RENDER_DATA);
        writePayload(NET_RENDER_DATA, new PacketBufferBC(buf), world.isRemote ? Side.CLIENT : Side.SERVER);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        NBTTagCompound nbt = super.getUpdateTag();
        nbt.setByteArray("d", bytes);
        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.readFromNBT(tag);
        byte[] bytes = tag.getByteArray("d");
        ByteBuf buf = Unpooled.copiedBuffer(bytes);

        try {
            int id = buf.readUnsignedShort();
            readPayload(id, new PacketBufferBC(buf), world.isRemote ? Side.CLIENT : Side.SERVER, null);
            // Make sure that we actually read the entire message rather than just discarding it
            MessageUtil.ensureEmpty(buf, world.isRemote, getClass() + ", id = " + getIdAllocator().getNameFor(id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        spawnReceiveParticles();
    }

    private void spawnReceiveParticles() {
        if (DEBUG_PARTICLES) {
            if (world != null) {
                double x = getPos().getX() + 0.5;
                double y = getPos().getY() + 0.5;
                double z = getPos().getZ() + 0.5;
                double dx = Math.random() - 0.5;
                double dy = Math.random() - 1;
                double dz = Math.random() - 0.5;
                world.spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, x, y, z, dx, dy, dz);
            }
        }
    }

    @Override
    public final IMessage receivePayload(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        int id = buffer.readUnsignedShort();
        readPayload(id, buffer, ctx.side, ctx);

        // Make sure that we actually read the entire message rather than just discarding it
        MessageUtil.ensureEmpty(buffer, world.isRemote, getClass() + ", id = " + getIdAllocator().getNameFor(id));

        if (ctx.side == Side.CLIENT) {
            spawnReceiveParticles();
        }
        return null;
    }

    // ######################
    //
    // Network overridables
    //
    // ######################

    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        // write render data with gui data
        if (id == NET_GUI_DATA) {

            writePayload(NET_RENDER_DATA, buffer, side);

            if (side == Side.SERVER) {
                MessageUtil.writeGameProfile(buffer, owner);
            }
        }
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                deltaManager.writeDeltaState(false, buffer);
            } else if (id == NET_GUI_DATA) {
                deltaManager.writeDeltaState(true, buffer);
            }
        }
    }

    /** @param ctx The context. Will be null if this is a generic update payload
     * @throws IOException if something went wrong */
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        // read render data with gui data
        if (id == NET_GUI_DATA) {
            readPayload(NET_RENDER_DATA, buffer, side, ctx);

            if (side == Side.CLIENT) {
                owner = MessageUtil.readGameProfile(buffer);
            }
        }
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) deltaManager.receiveDeltaData(false, EnumDeltaMessage.CURRENT_STATE, buffer);
            else if (id == NET_GUI_DATA) deltaManager.receiveDeltaData(true, EnumDeltaMessage.CURRENT_STATE, buffer);
            else if (id == NET_REN_DELTA_SINGLE) deltaManager.receiveDeltaData(false, EnumDeltaMessage.ADD_SINGLE, buffer);
            else if (id == NET_GUI_DELTA_SINGLE) deltaManager.receiveDeltaData(true, EnumDeltaMessage.ADD_SINGLE, buffer);
            else if (id == NET_REN_DELTA_CLEAR) deltaManager.receiveDeltaData(false, EnumDeltaMessage.SET_VALUE, buffer);
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
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        int version = nbt.getInteger("data-version");
        if (version != BCVersion.CURRENT.dataVersion) {
            migrateOldNBT(version, nbt);
        }
        deltaManager.readFromNBT(nbt.getCompoundTag("deltas"));
        if (nbt.hasKey("owner")) {
            owner = NBTUtil.readGameProfileFromNBT(nbt.getCompoundTag("owner"));
            if (owner != null && !owner.isComplete()) {
                owner = null;
            }
        }
        if (nbt.hasKey("items", Constants.NBT.TAG_COMPOUND)) {
            itemManager.deserializeNBT(nbt.getCompoundTag("items"));
        }
    }

    protected void migrateOldNBT(int version, NBTTagCompound nbt) {}

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("data-version", BCVersion.CURRENT.dataVersion);
        nbt.setTag("deltas", deltaManager.writeToNBT());
        if (owner != null && owner.isComplete()) {
            nbt.setTag("owner", NBTUtil.writeGameProfile(new NBTTagCompound(), owner));
        }
        NBTTagCompound items = itemManager.serializeNBT();
        if (!items.hasNoTags()) {
            nbt.setTag("items", items);
        }

        return nbt;
    }

    @Override
    protected void setWorldCreate(World world) {
        // The default impl doesn't actually set the world for some reason :/
        setWorld(world);
    }

    // ##################
    //
    // Advanced debugging
    //
    // ##################

    public boolean isBeingDebugged() {
        return BCAdvDebugging.isBeingDebugged(this);
    }

    public void enableDebugging() {
        if (getWorld() == null || getWorld().isRemote) {
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
        return hasWorld() && getWorld().getTileEntity(getPos()) == this;
    }

    @Override
    public void sendDebugState() {
        sendNetworkUpdate(NET_ADV_DEBUG);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IDetachedRenderer getDebugRenderer() {
        return null;
    }
}
