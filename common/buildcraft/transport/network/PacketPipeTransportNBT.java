package buildcraft.transport.network;

import buildcraft.core.network.PacketCoordinates;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class PacketPipeTransportNBT extends PacketCoordinates {

	private NBTTagCompound tag;
	private int entityId;

	public PacketPipeTransportNBT() {
	}

	public PacketPipeTransportNBT(int id, int x, int y, int z, int entityId, NBTTagCompound tag) {
		super(id, x, y, z);
		this.entityId = entityId;
		this.tag = tag;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);

		data.writeInt(entityId);
		byte[] compressed = CompressedStreamTools.compress(tag);
		data.writeShort(compressed.length);
		data.write(compressed);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);

		this.entityId = data.readInt();
		short length = data.readShort();
		byte[] compressed = new byte[length];
		data.readFully(compressed);
		this.tag = CompressedStreamTools.decompress(compressed);
	}

	public int getEntityId() {
		return entityId;
	}

	public NBTTagCompound getTagCompound() {
		return tag;
	}
}
