package buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketUpdate extends BuildCraftPacket {

	private int packetId;
	public int posX;
	public int posY;
	public int posZ;
	public PacketPayload payload;

	public PacketUpdate() {
	}

	public PacketUpdate(int packetId, PacketPayload payload) {
		this(packetId, 0, 0, 0, payload);
	}

	public PacketUpdate(int packetId, int posX, int posY, int posZ, PacketPayload payload) {
		this(packetId);

		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;

		this.payload = payload;
	}

	public PacketUpdate(int packetId) {
		this.packetId = packetId;
		this.isChunkDataPacket = true;

	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {

		data.writeInt(posX);
		data.writeInt(posY);
		data.writeInt(posZ);

		if (payload != null) {
			data.writeByte(payload.getType().ordinal());
			payload.writeData(data);
		} else {
			data.writeByte(0);
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {

		posX = data.readInt();
		posY = data.readInt();
		posZ = data.readInt();

		byte type = data.readByte();

		payload = PacketPayload.makePayload(type);

		if (payload != null)
			payload.readData(data);
	}

	@Override
	public int getID() {
		return packetId;
	}
}
