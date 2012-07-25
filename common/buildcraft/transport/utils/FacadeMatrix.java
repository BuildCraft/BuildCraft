package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import buildcraft.api.core.Orientations;


public class FacadeMatrix extends ConnectionMatrix {
	private String[] _textureFiles = new String[Orientations.dirs().length];
	private int[] _textureIndex = new int[Orientations.dirs().length];
	
	private boolean dirty = false;
	
	public FacadeMatrix() {
		for (Orientations direction : Orientations.dirs()){
			_textureFiles[direction.ordinal()] = "";
		}
	}
	
	public String getTextureFile(Orientations direction){
		return _textureFiles[direction.ordinal()];
	}
	
	public void setTextureFile(Orientations direction, String filePath){
		if (!_textureFiles[direction.ordinal()].equals(filePath)){
			_textureFiles[direction.ordinal()] = filePath;
			dirty = true;
		}
	}
	
	public int getTextureIndex(Orientations direction){
		return _textureIndex[direction.ordinal()];
	}
	
	public void setTextureIndex(Orientations direction, int value){
		if (_textureIndex[direction.ordinal()] != value){
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
		for (int i = 0; i < Orientations.dirs().length; i++){
			_textureFiles[i] = data.readUTF();
			_textureIndex[i] = data.readInt();
		}
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		for (int i = 0; i < Orientations.dirs().length; i++){
			data.writeUTF(_textureFiles[i]);
			data.writeInt(_textureIndex[i]);
		}
	}
}
