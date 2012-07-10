package net.minecraft.src.buildcraft.transport.utils;

import net.minecraft.src.buildcraft.api.Orientations;

public class ConnectionMatrix {
	
	private final boolean[] _connected = new boolean[Orientations.dirs().length];
	
	public void reset(){
		for (int i = 0; i < Orientations.dirs().length; i++){
			_connected[i] = false;
		}
	}
	
	public boolean isConnected(Orientations direction){
		return _connected[direction.ordinal()];
	}
	
	public void setConnected(Orientations direction, boolean value){
		_connected[direction.ordinal()] = value;
	}

}
