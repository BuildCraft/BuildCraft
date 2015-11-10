/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import buildcraft.BuildCraftTransport;
import buildcraft.core.lib.network.base.Packet;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;

import io.netty.buffer.ByteBuf;

public class PacketPipeTransportItemStackRequest extends Packet {

    public int travelerID;
    TravelingItem item;

    public PacketPipeTransportItemStackRequest() {

    }

    public PacketPipeTransportItemStackRequest(TileGenericPipe tile, PacketPipeTransportTraveler packet) {
        this.tempWorld = tile.getWorld();
        this.travelerID = packet.getTravelingEntityId();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeShort(travelerID);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        travelerID = data.readShort();
        TravelingItem.TravelingItemCache cache = TravelingItem.serverCache;
        item = cache.get(travelerID);
    }

    public void sendDataToPlayer(EntityPlayer player) {
        if (item != null) {
            Packet packet = new PacketPipeTransportItemStack(player.worldObj, travelerID, item.getItemStack());
            BuildCraftTransport.instance.sendToPlayer(player, packet);
        }
    }

    @Override
    public void applyData(World world, EntityPlayer player) {
        sendDataToPlayer(player);
    }
}
