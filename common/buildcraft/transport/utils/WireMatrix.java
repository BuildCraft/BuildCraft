package buildcraft.transport.utils;

import buildcraft.transport.Pipe;
import buildcraft.api.transport.PipeWire;
import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;

import net.minecraftforge.common.util.ForgeDirection;

public class WireMatrix {

	//private final boolean[] _hasWire = new boolean[IPipe.WireColor.values().length];
	private final BitSet _hasWire = new BitSet(PipeWire.values().length);
	private final BitSetCodec _bitSetCodec = new BitSetCodec();
	
	private final ConnectionMatrix _wires[] = new ConnectionMatrix[PipeWire.values().length];
	private final int[] _wireIconIndex = new int[PipeWire.values().length]; 
	
	private boolean dirty = false;

	public WireMatrix() {
		for (int i = 0; i < PipeWire.values().length; i++) {
			_wires[i] = new ConnectionMatrix();
		}
	}

	public boolean hasWire(PipeWire color) {
		return _hasWire.get(color.ordinal());
	}

	public void setWire(PipeWire color, boolean value) {
		if (_hasWire.get(color.ordinal()) != value) {
			_hasWire.set(color.ordinal(), value);
			dirty = true;
		}
	}

	public boolean isWireConnected(PipeWire color, ForgeDirection direction) {
		return _wires[color.ordinal()].isConnected(direction);
	}

	public void setWireConnected(PipeWire color, ForgeDirection direction, boolean value) {
		_wires[color.ordinal()].setConnected(direction, value);
	}
	
	public int getWireIconIndex(PipeWire color){
		return _wireIconIndex[color.ordinal()];
	}
	
	public void setWireIndex(PipeWire color, int value){
		if (_wireIconIndex[color.ordinal()] != value){
			_wireIconIndex[color.ordinal()] = value;
			dirty = true;
		}
	}

	public boolean isDirty() {

		for (int i = 0; i < PipeWire.values().length; i++) {
			if (_wires[i].isDirty())
				return true;
		}

		return dirty;
	}

	public void clean() {
		for (int i = 0; i < PipeWire.values().length; i++) {
			_wires[i].clean();
		}
		dirty = false;
	}

	public void writeData(ByteBuf data) {
		data.writeByte(_bitSetCodec.encode(_hasWire));

		for (int i = 0; i < PipeWire.values().length; i++) {
			_wires[i].writeData(data);
			data.writeByte(_wireIconIndex[i]);
		}
	}

	public void readData(ByteBuf data) {
		_bitSetCodec.decode(data.readByte(), _hasWire);
		for (int i = 0; i < PipeWire.values().length; i++) {
			_wires[i].readData(data);
			_wireIconIndex[i] = data.readByte();
		}
	}
}
