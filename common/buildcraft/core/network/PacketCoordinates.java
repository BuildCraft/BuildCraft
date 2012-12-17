package buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketCoordinates extends BuildCraftPacket {

	private int id;

	public int posX;
	public int posY;
	public int posZ;

	public PacketCoordinates() {
	}

	public PacketCoordinates(int id, int x, int y, int z) {

		this.id = id;

		this.posX = x;
		this.posY = y;
		this.posZ = z;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {

		data.writeInt(posX);
		data.writeInt(posY);
		data.writeInt(posZ);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {

		posX = data.readInt();
		posY = data.readInt();
		posZ = data.readInt();

	}

	@Override
	public int getID() {
		return id;
	}

}
