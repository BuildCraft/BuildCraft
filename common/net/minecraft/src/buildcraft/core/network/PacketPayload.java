package net.minecraft.src.buildcraft.core.network;

import net.minecraft.src.buildcraft.core.Utils;

public class PacketPayload {
	public int[] intPayload = new int[0];
	public float[] floatPayload = new float[0];
	public String[] stringPayload = new String[0];

	public PacketPayload() {}
	public PacketPayload(int intSize, int floatSize, int stringSize) {
		intPayload = new int[intSize];
		floatPayload = new float[floatSize];
		stringPayload = new String[stringSize];
	}

	public void append(PacketPayload other) {
		if(other == null)
			return;

		if(other.intPayload.length > 0)
			this.intPayload = Utils.concat(this.intPayload, other.intPayload);
		if(other.floatPayload.length > 0)
			this.floatPayload = Utils.concat(this.floatPayload, other.floatPayload);
		if(other.stringPayload.length > 0)
			this.stringPayload = Utils.concat(this.stringPayload, other.stringPayload);

	}

	public void append(int[] other) {
		if(other == null || other.length < 0)
			return;

		this.intPayload = Utils.concat(this.intPayload, other);
	}

	public void splitTail(IndexInPayload index) {
		PacketPayload payload = new PacketPayload(
				intPayload.length - index.intIndex,
				floatPayload.length - index.floatIndex,
				stringPayload.length - index.stringIndex);

		if(intPayload.length > 0)
			System.arraycopy(intPayload, index.intIndex, payload.intPayload, 0, payload.intPayload.length);
		if(floatPayload.length > 0)
			System.arraycopy(floatPayload, index.floatIndex, payload.floatPayload, 0, payload.floatPayload.length);
		if(stringPayload.length > 0)
			System.arraycopy(stringPayload, index.stringIndex, payload.stringPayload, 0, payload.stringPayload.length);
	}
}
