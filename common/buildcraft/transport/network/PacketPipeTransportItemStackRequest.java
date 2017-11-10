/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;

import buildcraft.BuildCraftTransport;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.TravelingItem;

public class PacketPipeTransportItemStackRequest extends Packet {

	public int travelerID;
	TravelingItem item;

	public PacketPipeTransportItemStackRequest() {

	}

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

	public void sendDataToPlayer(EntityPlayer player) {
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
