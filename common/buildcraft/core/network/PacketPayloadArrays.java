/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import buildcraft.core.utils.Utils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class PacketPayloadArrays extends PacketPayload {

	public int[] intPayload = new int[0];
	public float[] floatPayload = new float[0];
	public String[] stringPayload = new String[0];

	public PacketPayloadArrays() {
	}

	public PacketPayloadArrays(int intSize, int floatSize, int stringSize) {
		intPayload = new int[intSize];
		floatPayload = new float[floatSize];
		stringPayload = new String[stringSize];
	}

	public void append(PacketPayloadArrays other) {
		if (other == null)
			return;

		if (other.intPayload.length > 0) {
			this.intPayload = Utils.concat(this.intPayload, other.intPayload);
		}
		if (other.floatPayload.length > 0) {
			this.floatPayload = Utils.concat(this.floatPayload, other.floatPayload);
		}
		if (other.stringPayload.length > 0) {
			this.stringPayload = Utils.concat(this.stringPayload, other.stringPayload);
		}

	}

	public void append(int[] other) {
		if (other == null || other.length < 0)
			return;

		this.intPayload = Utils.concat(this.intPayload, other);
	}

	public void splitTail(IndexInPayload index) {
		PacketPayloadArrays payload = new PacketPayloadArrays(intPayload.length - index.intIndex, floatPayload.length - index.floatIndex, stringPayload.length
				- index.stringIndex);

		if (intPayload.length > 0) {
			System.arraycopy(intPayload, index.intIndex, payload.intPayload, 0, payload.intPayload.length);
		}
		if (floatPayload.length > 0) {
			System.arraycopy(floatPayload, index.floatIndex, payload.floatPayload, 0, payload.floatPayload.length);
		}
		if (stringPayload.length > 0) {
			System.arraycopy(stringPayload, index.stringIndex, payload.stringPayload, 0, payload.stringPayload.length);
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(intPayload.length);
		data.writeInt(floatPayload.length);
		data.writeInt(stringPayload.length);

		for (int intData : intPayload) {
			data.writeInt(intData);
		}
		for (float floatData : floatPayload) {
			data.writeFloat(floatData);
		}
		for (String stringData : stringPayload) {
			data.writeUTF(stringData);
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		intPayload = new int[data.readInt()];
		floatPayload = new float[data.readInt()];
		stringPayload = new String[data.readInt()];

		for (int i = 0; i < intPayload.length; i++) {
			intPayload[i] = data.readInt();
		}
		for (int i = 0; i < floatPayload.length; i++) {
			floatPayload[i] = data.readFloat();
		}
		for (int i = 0; i < stringPayload.length; i++) {
			stringPayload[i] = data.readUTF();
		}
	}

	@Override
	public Type getType() {
		return Type.ARRAY;
	}
}
