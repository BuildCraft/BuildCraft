package buildcraft.transport.network;

import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.core.utils.Utils;
import buildcraft.transport.TravelingItem;
import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;

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
		if (item != null)
			item.setItemStack(stack);
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
