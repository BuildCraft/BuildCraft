package buildcraft.transport.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import buildcraft.core.network.PacketCoordinates;

public class PacketSimpleId extends PacketCoordinates {

	public int entityId;
	public PacketSimpleId() {
		super();
	}

	public PacketSimpleId(int id, int x, int y, int z, int entityId) {
		super(id, x, y, z);
		this.entityId = entityId;
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(entityId);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		entityId = data.readInt();
	}
}
