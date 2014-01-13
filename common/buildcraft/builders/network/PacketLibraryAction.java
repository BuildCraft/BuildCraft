package buildcraft.builders.network;

import buildcraft.core.network.PacketCoordinates;
import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketLibraryAction extends PacketCoordinates {

	public int actionId;

	public PacketLibraryAction() {
	}

	public PacketLibraryAction(int packetId, int x, int y, int z) {
		super(packetId, x, y, z);
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeInt(actionId);
		super.writeData(data);
	}

	@Override
	public void readData(ByteBuf data) {
		actionId = data.readInt();
		super.readData(data);
	}
}
