package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraftforge.common.ForgeDirection;

public class FacadeMatrix extends ConnectionMatrix {
	private String[] _textureFiles = new String[ForgeDirection.VALID_DIRECTIONS.length];
	private int[] _textureIndex = new int[ForgeDirection.VALID_DIRECTIONS.length];

	private boolean dirty = false;

	public FacadeMatrix() {
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			_textureFiles[direction.ordinal()] = "";
		}
	}

	public String getTextureFile(ForgeDirection direction) {
		return _textureFiles[direction.ordinal()];
	}

	public void setTextureFile(ForgeDirection direction, String filePath) {
		if (!_textureFiles[direction.ordinal()].equals(filePath)) {
			_textureFiles[direction.ordinal()] = filePath;
			dirty = true;
		}
	}

	public int getTextureIndex(ForgeDirection direction) {
		return _textureIndex[direction.ordinal()];
	}

	public void setTextureIndex(ForgeDirection direction, int value) {
		if (_textureIndex[direction.ordinal()] != value) {
			_textureIndex[direction.ordinal()] = value;
			dirty = true;
		}
	}

	@Override
	public boolean isDirty() {
		return dirty || super.isDirty();
	}

	@Override
	public void clean() {
		super.clean();
		dirty = false;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			_textureFiles[i] = data.readUTF();
			_textureIndex[i] = data.readInt();
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.writeUTF(_textureFiles[i]);
			data.writeInt(_textureIndex[i]);
		}
	}
}
