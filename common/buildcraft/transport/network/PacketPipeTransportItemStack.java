package buildcraft.transport.network;

import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.TravelingItem;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;

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
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(entityId);
		Packet.writeItemStack(stack, data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		this.entityId = data.readInt();
		stack = Packet.readItemStack(data);
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
