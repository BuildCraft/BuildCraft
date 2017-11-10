/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.utils;

import java.util.BitSet;

import io.netty.buffer.ByteBuf;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.PipeWire;

public class WireMatrix {
	private final BitSet hasWire = new BitSet(PipeWire.values().length);
	private final BitSetCodec bitSetCodec = new BitSetCodec();

	private final ConnectionMatrix[] wires = new ConnectionMatrix[PipeWire.values().length];
	private final int[] wireIconIndex = new int[PipeWire.values().length];

	private boolean dirty = false;

	public WireMatrix() {
		for (int i = 0; i < PipeWire.values().length; i++) {
			wires[i] = new ConnectionMatrix();
		}
	}

	public boolean hasWire(PipeWire color) {
		return hasWire.get(color.ordinal());
	}

	public void setWire(PipeWire color, boolean value) {
		if (hasWire.get(color.ordinal()) != value) {
			hasWire.set(color.ordinal(), value);
			dirty = true;
		}
	}

	public boolean isWireConnected(PipeWire color, ForgeDirection direction) {
		return wires[color.ordinal()].isConnected(direction);
	}

	public void setWireConnected(PipeWire color, ForgeDirection direction, boolean value) {
		wires[color.ordinal()].setConnected(direction, value);
	}

	public int getWireIconIndex(PipeWire color) {
		return wireIconIndex[color.ordinal()];
	}

	public void setWireIndex(PipeWire color, int value) {
		if (wireIconIndex[color.ordinal()] != value) {
			wireIconIndex[color.ordinal()] = value;
			dirty = true;
		}
	}

	public boolean isDirty() {

		for (int i = 0; i < PipeWire.values().length; i++) {
			if (wires[i].isDirty()) {
				return true;
			}
		}

		return dirty;
	}

	public void clean() {
		for (int i = 0; i < PipeWire.values().length; i++) {
			wires[i].clean();
		}
		dirty = false;
	}

	public void writeData(ByteBuf data) {
		data.writeByte(bitSetCodec.encode(hasWire));

		for (int i = 0; i < PipeWire.values().length; i++) {
			wires[i].writeData(data);
			data.writeByte(wireIconIndex[i]);
		}
	}

	public void readData(ByteBuf data) {
		bitSetCodec.decode(data.readByte(), hasWire);
		for (int i = 0; i < PipeWire.values().length; i++) {
			wires[i].readData(data);
			wireIconIndex[i] = data.readUnsignedByte();
		}
	}
}
