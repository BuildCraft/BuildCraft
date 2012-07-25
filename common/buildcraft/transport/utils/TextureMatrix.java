package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import buildcraft.api.core.Orientations;


public class TextureMatrix {
	
	private final int[] _textureIndexes = new int[Orientations.values().length];
	
	private boolean dirty = false;
	
	public int getTextureIndex(Orientations direction){
		return _textureIndexes[direction.ordinal()];
	}
	
	public void setTextureIndex(Orientations direction, int value){
		if (_textureIndexes[direction.ordinal()] != value){
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
		for(int i = 0; i < Orientations.values().length; i++){
			data.writeInt(_textureIndexes[i]);
		}
	}

	public void readData(DataInputStream data) throws IOException {
		for (int i = 0; i < Orientations.values().length; i++){
			_textureIndexes[i] = data.readInt();
		}
	}
}
