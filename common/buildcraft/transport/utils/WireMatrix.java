package buildcraft.transport.utils;

import buildcraft.api.transport.IPipe;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.BitSet;
import net.minecraftforge.common.ForgeDirection;

public class WireMatrix {

	//private final boolean[] _hasWire = new boolean[IPipe.WireColor.values().length];
	private final BitSet _hasWire = new BitSet(IPipe.WireColor.values().length);
	private final BitSetCodec _bitSetCodec = new BitSetCodec();
	
	private final ConnectionMatrix _wires[] = new ConnectionMatrix[IPipe.WireColor.values().length];
	private final int[] _wireIconIndex = new int[IPipe.WireColor.values().length]; 
	
	private boolean dirty = false;

	public WireMatrix() {
		for (int i = 0; i < IPipe.WireColor.values().length; i++) {
			_wires[i] = new ConnectionMatrix();
		}
	}

	public boolean hasWire(IPipe.WireColor color) {
		return _hasWire.get(color.ordinal());
	}

	public void setWire(IPipe.WireColor color, boolean value) {
		if (_hasWire.get(color.ordinal()) != value) {
			_hasWire.set(color.ordinal(), value);
			dirty = true;
		}
	}

	public boolean isWireConnected(IPipe.WireColor color, ForgeDirection direction) {
		return _wires[color.ordinal()].isConnected(direction);
	}

	public void setWireConnected(IPipe.WireColor color, ForgeDirection direction, boolean value) {
		_wires[color.ordinal()].setConnected(direction, value);
	}
	
	public int getWireIconIndex(IPipe.WireColor color){
		return _wireIconIndex[color.ordinal()];
	}
	
	public void setWireIndex(IPipe.WireColor color, int value){
		if (_wireIconIndex[color.ordinal()] != value){
			_wireIconIndex[color.ordinal()] = value;
			dirty = true;
		}
	}

	public boolean isDirty() {

		for (int i = 0; i < IPipe.WireColor.values().length; i++) {
			if (_wires[i].isDirty())
				return true;
		}

		return dirty;
	}

	public void clean() {
		for (int i = 0; i < IPipe.WireColor.values().length; i++) {
			_wires[i].clean();
		}
		dirty = false;
	}

	public void writeData(DataOutputStream data) throws IOException {
		data.writeByte(_bitSetCodec.encode(_hasWire));

		for (int i = 0; i < IPipe.WireColor.values().length; i++) {
			_wires[i].writeData(data);
			data.writeByte(_wireIconIndex[i]);
		}
	}

	public void readData(DataInputStream data) throws IOException {
		_bitSetCodec.decode(data.readByte(), _hasWire);
		for (int i = 0; i < IPipe.WireColor.values().length; i++) {
			_wires[i].readData(data);
			_wireIconIndex[i] = data.readByte();
		}
	}
}
