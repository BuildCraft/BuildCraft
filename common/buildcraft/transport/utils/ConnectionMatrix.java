package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;
import net.minecraftforge.common.ForgeDirection;

public class ConnectionMatrix {

	private BitSet _connected = new BitSet(ForgeDirection.VALID_DIRECTIONS.length);
	private final BitSetCodec _bitSetCodec = new BitSetCodec();
	private boolean dirty = false;

	public boolean isConnected(ForgeDirection direction) {
		return _connected.get(direction.ordinal());
	}

	public void setConnected(ForgeDirection direction, boolean value) {
		if (_connected.get(direction.ordinal()) != value) {
			_connected.set(direction.ordinal(), value);
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
		data.writeByte(_bitSetCodec.encode(_connected));
	}

	public void readData(DataInputStream data) throws IOException {
		BitSet connection = new BitSet(ForgeDirection.VALID_DIRECTIONS.length);
		_bitSetCodec.decode(data.readByte(), connection);
		if (!_connected.equals(connection)) {
			_connected = connection;
			dirty = true;
		}
	}
}
