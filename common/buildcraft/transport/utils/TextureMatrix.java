package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraftforge.common.ForgeDirection;

public class TextureMatrix {

	private final int[] _iconIndexes = new int[7];
	private boolean dirty = false;

	public int getTextureIndex(ForgeDirection direction) {
		return _iconIndexes[direction.ordinal()];
	}

	public void setIconIndex(ForgeDirection direction, int value) {
		if (_iconIndexes[direction.ordinal()] != value) {
			_iconIndexes[direction.ordinal()] = value;
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
		for (int i = 0; i < _iconIndexes.length; i++) {
			data.writeByte(_iconIndexes[i]);
		}
	}

	public void readData(DataInputStream data) throws IOException {
		for (int i = 0; i < _iconIndexes.length; i++) {
			int icon = data.readByte();
			if (_iconIndexes[i] != icon) {
				_iconIndexes[i] = icon;
				dirty = true;
			}
		}
	}
}
