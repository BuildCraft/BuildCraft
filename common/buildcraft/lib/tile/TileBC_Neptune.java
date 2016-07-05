/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.tile;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;
import buildcraft.lib.TagManager.EnumTagTypeMulti;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.delta.DeltaManager.EnumDeltaMessage;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.PermissionUtil;
import buildcraft.lib.misc.PermissionUtil.PermissionBlock;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.command.IPayloadReceiver;
import buildcraft.lib.net.command.IPayloadWriter;
import buildcraft.lib.permission.PlayerOwner;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class TileBC_Neptune extends TileEntity implements IPayloadReceiver {
    /** Used for sending all data used for rendering the tile on a client. This does not include items, power, stages,
     * etc (Unless some are shown in the world) */
    public static final int NET_RENDER_DATA = 0;
    /** Used for sending all data in the GUI. Basically what has been omitted from {@link #NET_RENDER_DATA} that is
     * shown in the GUI. */
    public static final int NET_GUI_DATA = 1;

    public static final int NET_REN_DELTA_SINGLE = 2;
    public static final int NET_REN_DELTA_CLEAR = 3;
    public static final int NET_GUI_DELTA_SINGLE = 4;
    public static final int NET_GUI_DELTA_CLEAR = 5;

    /** Handles all of the players that are currently using this tile (have a GUI open) */
    private final Set<EntityPlayer> usingPlayers = Sets.newIdentityHashSet();
    private PlayerOwner owner;

    protected final DeltaManager deltaManager = new DeltaManager((gui, type, writer) -> {
        final int netId;
        if (type == EnumDeltaMessage.ADD_SINGLE) {
            netId = gui ? NET_GUI_DELTA_SINGLE : NET_REN_DELTA_SINGLE;
        } else if (type == EnumDeltaMessage.SET_VALUE) {
            netId = gui ? NET_GUI_DELTA_CLEAR : NET_REN_DELTA_CLEAR;
        } else {
            throw new IllegalArgumentException("Unknown delta message type " + type);
        }
        this.createAndSendMessage(gui, netId, writer);
    });

    public TileBC_Neptune() {}

    public static <T extends TileBC_Neptune> void registerTile(Class<T> tileClass, String id) {
        String regName = TagManager.getTag(id, EnumTagType.REGISTRY_NAME);
        String[] alternatives = TagManager.getMultiTag(id, EnumTagTypeMulti.OLD_REGISTRY_NAME);
        GameRegistry.registerTileEntityWithAlternatives(tileClass, regName, alternatives);
    }

    /** Checks to see if this tile can update. The base implementation only checks to see if it has a world. */
    public boolean cannotUpdate() {
        return !hasWorldObj();
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

    /** Called whenever the block is removed. called by {@link #onExplode(Explosion)}, and
     * {@link Block#breakBlock(World, BlockPos, IBlockState)} */
    public void onRemove() {}

    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        if (!placer.worldObj.isRemote) {
            this.owner = PlayerOwner.getOwnerOf(placer);
        }
    }

    public void onPlayerOpen(EntityPlayer player) {
        usingPlayers.add(player);
        sendNetworkUpdate(NET_GUI_DATA, player);
    }

    public void onPlayerClose(EntityPlayer player) {
        usingPlayers.remove(player);
    }

    public boolean canInteractWith(EntityPlayer player) {
        if (worldObj.getTileEntity(getPos()) != this) {
            return false;
        }
        if (player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) > 64.0D) {
            return false;
        }
        // edit rather than view because you can normally change the contents from gui interaction
        return PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, getPermBlock());
    }

    public PlayerOwner getOwner() {
        return owner;
    }

    public PermissionUtil.PermissionBlock getPermBlock() {
        return new PermissionBlock(getOwner(), getPos());
    }

    // ##################
    //
    // Network helpers
    //
    // ##################

    /** Tells MC to redraw this block. Note that (in 1.9) this ALSO sends a description packet. */
    public final void redrawBlock() {
        if (hasWorldObj()) {
            IBlockState state = worldObj.getBlockState(getPos());
            worldObj.notifyBlockUpdate(pos, state, state, 0);
        }
    }

    /** Sends a network update update of the specified ID. */
    public final void sendNetworkUpdate(int id) {
        if (hasWorldObj()) {
            MessageUpdateTile message = createNetworkUpdate(id);
            MessageUtil.sendToAllWatching(this.worldObj, this.getPos(), message);
        }
    }

    public final void sendNetworkUpdate(int id, EntityPlayer target) {
        if (hasWorldObj() && target instanceof EntityPlayerMP) {
            MessageUpdateTile message = createNetworkUpdate(id);
            MessageUtil.getWrapper().sendTo(message, (EntityPlayerMP) target);
        }
    }

    public final MessageUpdateTile createNetworkUpdate(final int id) {
        if (hasWorldObj()) {
            final Side side = worldObj.isRemote ? Side.CLIENT : Side.SERVER;
            return new MessageUpdateTile(getPos(), this.getClass().getName(), buffer -> {
                buffer.writeShort(id);
                this.writePayload(id, buffer, side);
            });
        }
        return null;
    }

    public final void createAndSendMessage(boolean gui, int id, IPayloadWriter writer) {
        if (hasWorldObj()) {
            MessageUpdateTile message = new MessageUpdateTile(getPos(), this.getClass().getName(), buffer -> {
                buffer.writeShort(id);
                writer.write(buffer);
            });
            if (gui) {
                MessageUtil.sendToPlayers(usingPlayers, message);
            } else {
                MessageUtil.sendToAllWatching(this.worldObj, this.getPos(), message);
            }
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        MessageUpdateTile message = createNetworkUpdate(NET_RENDER_DATA);
        MessageUtil.doDelayed(() -> MessageUtil.sendToAllWatching(worldObj, getPos(), message));
        return null;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        MessageUpdateTile message = createNetworkUpdate(NET_RENDER_DATA);
        ByteBuf buf = Unpooled.buffer();
        message.toBytes(buf);
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
            receivePayload(worldObj.isRemote ? Side.CLIENT : Side.SERVER, new PacketBuffer(buf));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final IMessage receivePayload(Side side, PacketBuffer buffer) throws IOException {
        int id = buffer.readUnsignedShort();
        readPayload(id, buffer, side);
        return null;
    }

    // ######################
    //
    // Network overridables
    //
    // ######################

    public void writePayload(int id, PacketBuffer buffer, Side side) {
        // Send a Render Data packet as well as the gui packet
        if (id == NET_GUI_DATA) {
            writePayload(NET_RENDER_DATA, buffer, side);
        }
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                deltaManager.writeDeltaState(false, buffer);
            } else if (id == NET_GUI_DATA) {
                deltaManager.writeDeltaState(true, buffer);
            }
        }
    }

    /** @throws IOException if something went wrong */
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        // Send a Render Data packet as well as the gui packet
        if (id == NET_GUI_DATA) {
            readPayload(NET_RENDER_DATA, buffer, side);
        }
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) deltaManager.receiveDeltaData(false, EnumDeltaMessage.CURRENT_STATE, buffer);
            else if (id == NET_GUI_DATA) deltaManager.receiveDeltaData(true, EnumDeltaMessage.CURRENT_STATE, buffer);
            else if (id == NET_REN_DELTA_SINGLE) deltaManager.receiveDeltaData(false, EnumDeltaMessage.ADD_SINGLE, buffer);
            else if (id == NET_GUI_DELTA_SINGLE) deltaManager.receiveDeltaData(true, EnumDeltaMessage.ADD_SINGLE, buffer);
            else if (id == NET_REN_DELTA_CLEAR) deltaManager.receiveDeltaData(false, EnumDeltaMessage.SET_VALUE, buffer);
            else if (id == NET_GUI_DELTA_CLEAR) deltaManager.receiveDeltaData(true, EnumDeltaMessage.SET_VALUE, buffer);

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
    }

    protected void migrateOldNBT(int version, NBTTagCompound nbt) {}

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("data-version", BCVersion.CURRENT.dataVersion);
        nbt.setTag("deltas", deltaManager.writeToNBT());
        return nbt;
    }
}
