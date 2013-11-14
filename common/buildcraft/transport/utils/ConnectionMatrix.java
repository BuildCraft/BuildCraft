package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraftforge.common.ForgeDirection;

public class ConnectionMatrix {
	private int mask = 0;
	private boolean dirty = false;

	public boolean isConnected(ForgeDirection direction) {
		// test if the direction.ordinal()'th bit of mask is set
		return (mask & (1 << direction.ordinal())) != 0;
	}

	public void setConnected(ForgeDirection direction, boolean value) {
		if (isConnected(direction) != value) {
			// invert the direction.ordinal()'th bit of mask
			mask ^= 1 << direction.ordinal();
			dirty = true;
		}
	}

	/**
	 * Return a mask representing the connectivity for all sides.
	 *
	 * @return mask in ForgeDirection order, least significant bit = first entry
	 */
	public int getMask() {
		return mask;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void clean() {
		dirty = false;
	}

	public void writeData(DataOutputStream data) throws IOException {
		data.writeByte(mask);
	}

	public void readData(DataInputStream data) throws IOException {
		byte newMask = data.readByte();

		if (newMask != mask) {
			mask = newMask;
			dirty = true;
		}
	}
}
