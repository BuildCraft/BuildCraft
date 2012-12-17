package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraftforge.common.ForgeDirection;

public class TextureMatrix {

	private final int[] _textureIndexes = new int[ForgeDirection.values().length];

	private boolean dirty = false;

	public int getTextureIndex(ForgeDirection direction) {
		return _textureIndexes[direction.ordinal()];
	}

	public void setTextureIndex(ForgeDirection direction, int value) {
		if (_textureIndexes[direction.ordinal()] != value) {
			_textureIndexes[direction.ordinal()] = value;
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
		for (int i = 0; i < ForgeDirection.values().length; i++) {
			data.writeInt(_textureIndexes[i]);
		}
	}

	public void readData(DataInputStream data) throws IOException {
		for (int i = 0; i < ForgeDirection.values().length; i++) {
			_textureIndexes[i] = data.readInt();
		}
	}
}
