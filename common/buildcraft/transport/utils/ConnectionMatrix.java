package buildcraft.transport.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import buildcraft.api.core.Orientations;


public class ConnectionMatrix {
	
	private final boolean[] _connected = new boolean[Orientations.dirs().length];
	
	private boolean dirty = false;
	
	public boolean isConnected(Orientations direction){
		return _connected[direction.ordinal()];
	}
	
	public void setConnected(Orientations direction, boolean value){
		if (_connected[direction.ordinal()] != value){
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
		for(int i = 0; i < Orientations.dirs().length; i++){
			data.writeBoolean(_connected[i]);
		}
	}

	public void readData(DataInputStream data) throws IOException {
		for (int i = 0; i < Orientations.dirs().length; i++){
			_connected[i] = data.readBoolean();
		}
	}
}
