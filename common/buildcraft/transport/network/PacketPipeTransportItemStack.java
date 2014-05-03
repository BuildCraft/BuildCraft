/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;

import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.utils.Utils;
import buildcraft.transport.TravelingItem;

public class PacketPipeTransportItemStack extends BuildCraftPacket {

	private ItemStack stack;
	private int entityId;

	public PacketPipeTransportItemStack() {
	}

	public PacketPipeTransportItemStack(int entityId, ItemStack stack) {
		this.entityId = entityId;
		this.stack = stack;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeInt(entityId);
		Utils.writeStack(data, stack);
	}

	@Override
	public void readData(ByteBuf data) {
		this.entityId = data.readInt();
		stack = Utils.readStack(data);
		TravelingItem item = TravelingItem.clientCache.get(entityId);
		if (item != null) {
			item.setItemStack(stack);
		}
	}

	public int getEntityId() {
		return entityId;
	}

	public ItemStack getItemStack() {
		return stack;
	}

	@Override
	public int getID() {
		return PacketIds.PIPE_ITEMSTACK;
	}
}
