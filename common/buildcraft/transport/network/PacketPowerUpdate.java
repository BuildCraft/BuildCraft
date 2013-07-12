package buildcraft.transport.network;

import buildcraft.core.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketPowerUpdate extends PacketCoordinates {

	public boolean overload;
	public short[] displayPower;

	public PacketPowerUpdate() {
	}

	public PacketPowerUpdate(int x, int y, int z) {
		super(PacketIds.PIPE_POWER, x, y, z);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		displayPower = new short[] { 0, 0, 0, 0, 0, 0 };
		super.readData(data);
		overload = data.readBoolean();
		for (int i = 0; i < displayPower.length; i++) {
			displayPower[i] = data.readByte();
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(overload);
		for (int i = 0; i < displayPower.length; i++) {
			data.writeByte(displayPower[i]);
		}
	}
}
