/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import java.util.LinkedList;

public class ByteBuffer {

	LinkedList<Integer> bytes = new LinkedList<Integer>();

	public void writeUnsignedByte(int s) {
		bytes.add(s - Byte.MAX_VALUE);
	}

	public void writeUnsignedShort(int s) {
		writeUnsignedByte(s & 0xFF);
		writeUnsignedByte((s >> 8) & 0xFF);
	}

	public void writeShort(short s) {
		writeUnsignedByte(s & 0xFF);
		writeUnsignedByte((s >> 8) & 0xFF);
	}

	public void writeInt(int i) {
		writeUnsignedByte(i & 0xFF);
		writeUnsignedByte((i >> 8) & 0xFF);
		writeUnsignedByte((i >> 16) & 0xFF);
		writeUnsignedByte((i >> 24) & 0xFF);
	}

	public short readUnsignedByte() {
		short res = 0;

		if (bytes.size() == 0)
			return res;

		res += (short) (bytes.removeFirst() + Byte.MAX_VALUE);

		return res;
	}

	public int readUnsignedShort() {
		int res = 0;

		if (bytes.size() == 0)
			return res;

		res += readUnsignedByte();
		res += readUnsignedByte() << 8;

		return res;
	}

	public short readShort() {
		short res = 0;

		if (bytes.size() == 0)
			return res;

		res += readUnsignedByte();
		res += readUnsignedByte() << 8;

		return res;
	}

	public int readInt() {
		int res = 0;

		if (bytes.size() == 0)
			return res;

		res += readUnsignedByte();
		res += readUnsignedByte() << 8;
		res += readUnsignedByte() << 16;
		res += readUnsignedByte() << 24;

		return res;
	}

	public int[] readIntArray() {
		LinkedList<Integer> ints = new LinkedList<Integer>();

		while (bytes.size() > 0) {
			ints.add(readInt());
		}

		int[] res = new int[ints.size()];

		int index = 0;

		for (Integer val : ints) {
			res[index] = val;
			index++;
		}

		return res;
	}

	public void writeIntArray(int[] arr) {
		for (int i : arr) {
			writeInt(i);
		}
	}
}
