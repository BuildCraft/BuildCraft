/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.tile;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.IPlayerOwned;

import buildcraft.lib.cap.CapabilityHelper;
import buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.IAdvDebugTarget;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.delta.DeltaManager.EnumDeltaMessage;
import buildcraft.lib.fluid.TankManager;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FakePlayerProvider;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.PermissionUtil;
import buildcraft.lib.misc.PermissionUtil.PermissionBlock;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.IPayloadReceiver;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerManager;

public abstract class TileBC_Neptune extends TileEntity implements IPayloadReceiver, IAdvDebugTarget, IPlayerOwned {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("tile.debug.network");

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
    protected final ItemHandlerManager itemManager = new ItemHandlerManager(this::onSlotChange);
    protected final TankManager tankManager = new TankManager();

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
            createAndSendGuiMessage(id, writer);
        } else {
            createAndSendMessage(id, writer);
        }
    });

    public TileBC_Neptune() {
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

    public final IBlockState getCurrentState() {
        return BlockUtil.getBlockState(world, pos);
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
        return getLocalState(pos.add(offset));
    }

    /** @param pos The <i>absolute</i> position of the {@link IBlockState} . */
    public final IBlockState getLocalState(BlockPos pos) {
        return BlockUtil.getBlockState(world, pos, true);
    }

    public final TileEntity getNeighbourTile(EnumFacing offset) {
        return getOffsetTile(offset.getDirectionVec());
    }

    /** @param offset The position of the {@link TileEntity} to retrieve, <i>relative</i> to this
     *            {@link TileEntity#getPos()} . */
    public final TileEntity getOffsetTile(Vec3i offset) {
        return getLocalTile(pos.add(offset));
    }

    /** @param pos The <i>absolute</i> position of the {@link TileEntity} . */
    public final TileEntity getLocalTile(BlockPos pos) {
        return BlockUtil.getTileEntity(world, pos, true);
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
    public void onRemove() {
        NonNullList<ItemStack> toDrop = NonNullList.create();
        addDrops(toDrop, 0);
        InventoryUtil.dropAll(world, pos, toDrop);
    }

    /** Called whenever {@link Block#getDrops(NonNullList, IBlockAccess, BlockPos, IBlockState, int)}, or
     * {@link #onRemove()} is called (by default). */
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        itemManager.addDrops(toDrop);
        tankManager.addDrops(toDrop);
    }

    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        if (!placer.world.isRemote) {
            if (placer instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) placer;
                owner = player.getGameProfile();
                if (!owner.isComplete()) {
                    throw new IllegalArgumentException("Incomplete owner! ( " + placer + " -> " + owner + " )");
                }
            } else {
                throw new IllegalArgumentException("Not an EntityPlayer! (placer = " + placer + ")");
            }
        }
    }

    public void onPlayerOpen(EntityPlayer player) {
        if (owner == null) {
            owner = player.getGameProfile();
            if (!owner.isComplete()) {
                throw new IllegalArgumentException("Incomplete owner! ( " + player + " -> " + owner + " )");
            }
        }
        sendNetworkUpdate(NET_GUI_DATA, player);
        usingPlayers.add(player);
    }

    public void onPlayerClose(EntityPlayer player) {
        usingPlayers.remove(player);
    }

    public boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY,
        float hitZ) {
        return tankManager.onActivated(player, getPos(), hand);
    }

    public void onNeighbourBlockChanged(Block block, BlockPos nehighbour) {

    }

    @Override
    public final boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        T obj = caps.getCapability(capability, facing);
        if (obj == null) {
            obj = super.getCapability(capability, facing);
        }
        return obj;
    }

    // Item caps
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before,
        @Nonnull ItemStack after) {
        if (world.isBlockLoaded(pos)) {
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
        if (owner == null) {
            String msg = "[lib.tile] Unknown owner for " + getClass() + " at ";
            BCLog.logger.warn(msg + StringUtilBC.blockPosToString(getPos()));
            return FakePlayerProvider.NULL_PROFILE;
        }
        return owner;
    }

    public PermissionUtil.PermissionBlock getPermBlock() {
        return new PermissionBlock(this, pos);
    }

    public boolean canEditOther(BlockPos other) {
        return PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, getPermBlock(),
            PermissionUtil.createFrom(world, other));
    }

    public boolean canPlayerEdit(EntityPlayer player) {
        return PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, getPermBlock());
    }

    public boolean canInteractWith(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) {
            return false;
        }
        if (player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
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

                if (DEBUG) {
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 0.5;
                    double z = pos.getZ() + 0.5;
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
            if (world.isRemote) {
                MessageManager.sendToServer(message);
            } else {
                MessageUtil.sendToAllWatching(world, pos, message);
            }
        }
    }

    public final void sendNetworkGuiTick(EntityPlayer player) {
        if (hasWorld() && !world.isRemote) {
            MessageUpdateTile message = createNetworkUpdate(NET_GUI_TICK);
            if (message.getPayloadSize() <= Short.BYTES) {
                return;
            }
            MessageManager.sendTo(message, (EntityPlayerMP) player);
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
            MessageManager.sendTo(message, (EntityPlayerMP) target);
        }
    }

    public final MessageUpdateTile createNetworkUpdate(final int id) {
        if (hasWorld()) {
            final Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
            return createMessage(id, (buffer) -> writePayload(id, buffer, side));
        } else {
            BCLog.logger.warn("Did not have a world at " + pos + "!");
        }
        return null;
    }

    public final void createAndSendMessage(int id, IPayloadWriter writer) {
        if (hasWorld()) {
            IMessage message = createMessage(id, writer);
            if (world.isRemote) {
                MessageManager.sendToServer(message);
            } else {
                MessageUtil.sendToAllWatching(world, pos, message);
            }
        }
    }

    public final void createAndSendGuiMessage(int id, IPayloadWriter writer) {
        if (hasWorld()) {
            IMessage message = createMessage(id, writer);
            if (world.isRemote) {
                MessageManager.sendToServer(message);
            } else {
                MessageUtil.sendToPlayers(usingPlayers, message);
            }
        }
    }

    public final void createAndSendMessage(int id, EntityPlayerMP player, IPayloadWriter writer) {
        if (hasWorld()) {
            IMessage message = createMessage(id, writer);
            MessageManager.sendTo(message, player);
        }
    }

    public final void createAndSendGuiMessage(int id, EntityPlayerMP player, IPayloadWriter writer) {
        if (usingPlayers.contains(player)) {
            createAndSendMessage(id, player, writer);
        }
    }

    public final MessageUpdateTile createMessage(int id, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        buffer.writeShort(id);
        writer.write(buffer);
        return new MessageUpdateTile(pos, buffer);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
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
            PacketBufferBC buffer = new PacketBufferBC(buf);
            readPayload(id, buffer, world.isRemote ? Side.CLIENT : Side.SERVER, null);
            // Make sure that we actually read the entire message rather than just discarding it
            MessageUtil.ensureEmpty(buffer, world.isRemote, getClass() + ", id = " + getIdAllocator().getNameFor(id));
            spawnReceiveParticles(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void spawnReceiveParticles(int id) {
        if (DEBUG) {
            String name = getIdAllocator().getNameFor(id);

            if (world != null) {
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;
                double r = 0.01 + (id & 3) / 4.0;
                double g = 0.01 + ((id / 4) & 3) / 4.0;
                double b = 0.01 + ((id / 16) & 3) / 4.0;
                world.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, r, g, b);
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
            spawnReceiveParticles(id);
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
            else if (id == NET_REN_DELTA_SINGLE)
                deltaManager.receiveDeltaData(false, EnumDeltaMessage.ADD_SINGLE, buffer);
            else if (id == NET_GUI_DELTA_SINGLE)
                deltaManager.receiveDeltaData(true, EnumDeltaMessage.ADD_SINGLE, buffer);
            else if (id == NET_REN_DELTA_CLEAR)
                deltaManager.receiveDeltaData(false, EnumDeltaMessage.SET_VALUE, buffer);
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
        migrateOldNBT(nbt.getInteger("data-version"), nbt);
        deltaManager.readFromNBT(nbt.getCompoundTag("deltas"));
        if (nbt.hasKey("owner")) {
            owner = NBTUtil.readGameProfileFromNBT(nbt.getCompoundTag("owner"));
            if (owner == null || !owner.isComplete()) {
                String msg = "[lib.tile] Unknown owner (" + owner + ") for " + getClass() + " at ";
                BCLog.logger.warn(msg + getPos() + " when reading from NBT");
            }
        } else {
            String msg = "[lib.tile] Unknown owner (null) for " + getClass() + " at ";
            BCLog.logger.warn(msg + getPos() + " when reading from NBT");
        }
        if (nbt.hasKey("items", Constants.NBT.TAG_COMPOUND)) {
            itemManager.deserializeNBT(nbt.getCompoundTag("items"));
        }
        if (nbt.hasKey("tanks", Constants.NBT.TAG_COMPOUND)) {
            tankManager.deserializeNBT(nbt.getCompoundTag("tanks"));
        }
    }

    protected void migrateOldNBT(int version, NBTTagCompound nbt) {
        // 7.99.0 -> 7.99.4
        // Most tiles with a single tank saved it under "tank"
        NBTTagCompound tankComp = nbt.getCompoundTag("tank");
        if (!tankComp.hasNoTags()) {
            NBTTagCompound tanks = new NBTTagCompound();
            tanks.setTag("tank", tankComp);
            nbt.setTag("tanks", tanks);
        }
    }

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
        NBTTagCompound tanks = tankManager.serializeNBT();
        if (!tanks.hasNoTags()) {
            nbt.setTag("tanks", tanks);
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
        if (world.isRemote) {
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
        return hasWorld() && world.getTileEntity(pos) == this;
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
