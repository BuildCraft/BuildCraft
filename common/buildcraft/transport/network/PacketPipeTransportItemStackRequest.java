package buildcraft.transport.network;

import buildcraft.BuildCraftTransport;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.TravelingItem;
import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

public class PacketPipeTransportItemStackRequest extends BuildCraftPacket {

	public int travelerID;
	TravelingItem item;
	
	public PacketPipeTransportItemStackRequest(int travelerID) {
		this.travelerID = travelerID;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeShort(travelerID);
	}

	@Override
	public void readData(ByteBuf data) {
		travelerID = data.readShort();
		TravelingItem.TravelingItemCache cache = TravelingItem.serverCache;
		item = cache.get(travelerID);		
	}

	public void sendDataToPlayer (EntityPlayer player) {
		if (item != null) {
			BuildCraftTransport.instance.sendToPlayer(
					player,
					new PacketPipeTransportItemStack(travelerID, item
							.getItemStack()));
		}
	}
	
	@Override
	public int getID() {
		return PacketIds.PIPE_ITEMSTACK_REQUEST;
	}
}
