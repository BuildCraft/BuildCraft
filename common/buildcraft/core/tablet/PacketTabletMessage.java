package buildcraft.core.tablet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.network.base.Packet;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.tablet.manager.TabletManagerClient;
import buildcraft.core.tablet.manager.TabletManagerServer;

import io.netty.buffer.ByteBuf;

public class PacketTabletMessage extends Packet {
    private NBTTagCompound tag;
    private int playerId;

    public PacketTabletMessage() {
        tag = new NBTTagCompound();
    }

    public PacketTabletMessage(NBTTagCompound tag, EntityPlayer player) {
        super(player.worldObj);
        this.tag = tag;
        this.playerId = player.getEntityId();
    }

    public NBTTagCompound getTag() {
        return tag;
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        playerId = data.readInt();
        this.tag = NetworkUtils.readNBT(data);
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeInt(playerId);
        int index = data.writerIndex();
        NetworkUtils.writeNBT(data, tag);
        index = data.writerIndex() - index;
        if (index > 65535) {
            BCLog.logger.error("NBT data is too large (" + index + " > 65,535)! Please report!");
        }
    }

    @Override
    public void applyData(World world, EntityPlayer player) {
        if (world.isRemote) {
            TabletBase tablet = TabletManagerClient.INSTANCE.get().getTablet();
            tablet.receiveMessage(getTag());
        } else {
            Entity playerById = world.getEntityByID(playerId);
            if (playerById instanceof EntityPlayer) {
                TabletBase tablet = TabletManagerServer.INSTANCE.get((EntityPlayer) playerById);
                tablet.receiveMessage(getTag());
            }
        }
    }
}
