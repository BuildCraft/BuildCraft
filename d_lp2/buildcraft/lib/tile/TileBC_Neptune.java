package buildcraft.lib.tile;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.TagManager;
import buildcraft.lib.TagManager.EnumTagType;
import buildcraft.lib.TagManager.EnumTagTypeMulti;
import buildcraft.lib.delta.DeltaManager;
import buildcraft.lib.delta.DeltaManager.EnumDeltaMessage;
import buildcraft.lib.migrate.BCVersion;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.command.IPayloadReceiver;
import buildcraft.lib.net.command.IPayloadWriter;

public abstract class TileBC_Neptune extends TileEntity implements IPayloadReceiver {
    /** Used for sending all data used for rendering the tile on a client. This does not include items, power, stages,
     * etc (Unless some are shown in the world) */
    public static final int NET_RENDER_DATA = 0;
    /** Used for sending all data in the GUI. Basically what has been omitted from {@link #NET_RENDER_DATA} that is
     * shown in the GUI. */
    public static final int NET_GUI_DATA = 1;

    public static final int NET_DELTA_SINGLE = 2;
    public static final int NET_DELTA_CLEAR = 3;
    public static final int NET_DELTA_ALL = 4;

    protected final DeltaManager deltaManager = new DeltaManager((type, writer) -> {
        final int netId;
        if (type == EnumDeltaMessage.ADD_SINGLE) {
            netId = NET_DELTA_SINGLE;
        } else if (type == EnumDeltaMessage.SET_VALUE) {
            netId = NET_DELTA_CLEAR;
        } else if (type == EnumDeltaMessage.CURRENT_STATE) {
            netId = NET_DELTA_ALL;
        } else {
            throw new IllegalArgumentException("Unknown delta message type " + type);
        }
        this.createAndSendMessage(netId, writer);
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

    // Network helpers
    /** Tells MC to redraw this block. Note that (in 1.9) this ALSO sends a description packet. */
    public void redrawBlock() {
        if (hasWorldObj()) worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    /** Sends a network update update of the specified ID. */
    public void sendNetworkUpdate(int id) {
        if (hasWorldObj()) {
            MessageUpdateTile message = createNetworkUpdate(id);
            MessageUtil.sendToAllWatching(this.worldObj, this.getPos(), message);
        }
    }

    public void sendNetworkUpdate(int id, EntityPlayer target) {
        if (hasWorldObj() && target instanceof EntityPlayerMP) {
            MessageUpdateTile message = createNetworkUpdate(id);
            MessageUtil.getWrapper().sendTo(message, (EntityPlayerMP) target);
        }
    }

    public final MessageUpdateTile createNetworkUpdate(final int id) {
        if (hasWorldObj()) {
            final Side side = worldObj.isRemote ? Side.CLIENT : Side.SERVER;
            return new MessageUpdateTile(getPos(), buffer -> {
                buffer.writeShort(id);
                this.writePayload(id, buffer, side);
            });
        }
        return null;
    }

    public final void createAndSendMessage(final int id, final IPayloadWriter writer) {
        if (hasWorldObj()) {
            MessageUpdateTile message = new MessageUpdateTile(getPos(), buffer -> {
                buffer.writeShort(id);
                writer.write(buffer);
            });
            MessageUtil.sendToAllWatching(this.worldObj, this.getPos(), message);
        }
    }

    @Override
    public final Packet<?> getDescriptionPacket() {
        MessageUpdateTile message = createNetworkUpdate(NET_RENDER_DATA);
        return BCMessageHandler.netWrapper.getPacketFrom(message);
    }

    @Override
    public final IMessage receivePayload(Side side, PacketBuffer buffer) throws IOException {
        int id = buffer.readUnsignedShort();
        readPayload(id, buffer, side);
        return null;
    }

    // Network overridables
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                deltaManager.writeDeltaState(buffer);
            }
        }
    }

    /** @throws IOException if something went wrong */
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                deltaManager.receiveDeltaData(EnumDeltaMessage.CURRENT_STATE, buffer);
            } else if (id == NET_DELTA_SINGLE) {
                deltaManager.receiveDeltaData(EnumDeltaMessage.ADD_SINGLE, buffer);
            } else if (id == NET_DELTA_CLEAR) {
                deltaManager.receiveDeltaData(EnumDeltaMessage.SET_VALUE, buffer);
            } else if (id == NET_DELTA_ALL) {
                deltaManager.receiveDeltaData(EnumDeltaMessage.CURRENT_STATE, buffer);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        int version = nbt.getInteger("data-version");
        if (version != BCVersion.CURRENT.dataVersion) {
            migrateOldNBT(nbt);
        }
        deltaManager.readFromNBT(nbt.getTagList("deltas", Constants.NBT.TAG_COMPOUND));
    }

    protected void migrateOldNBT(NBTTagCompound nbt) {

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("data-version", BCVersion.CURRENT.dataVersion);
        nbt.setTag("deltas", deltaManager.writeToNBT());
    }
}
