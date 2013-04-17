package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraftforge.common.ForgeDirection;

public class ConnectionMatrix {

	private final boolean[] _connected = new boolean[ForgeDirection.VALID_DIRECTIONS.length];

	private boolean dirty = false;

	public boolean isConnected(ForgeDirection direction) {
		return _connected[direction.ordinal()];
	}

	public void setConnected(ForgeDirection direction, boolean value) {
		if (_connected[direction.ordinal()] != value) {
			_connected[direction.ordinal()] = value;
			dirty = true;
		}
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void writeData(DataOutputStream data) throws IOException {
		byte write = 0;
		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			if (this._connected[i]){
				write |= 1 << i;
			}
		}
		data.writeByte(write);
	}

	public void readData(DataInputStream data) throws IOException {
		byte read = data.readByte();
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			_connected[i] = (read & (1 << i)) != 0;;
		}
	}
}
