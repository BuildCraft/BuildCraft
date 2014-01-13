package buildcraft.core.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class PacketPayload {

	public static enum Type {

		NULL, ARRAY, STREAM
	}

	public static PacketPayload makePayload(int type) {
		if (type == Type.ARRAY.ordinal())
			return new PacketPayloadArrays();
		if (type == Type.STREAM.ordinal())
			return new PacketPayloadStream();
		return null;
	}

	public abstract void writeData(ByteBuf data);

	public abstract void readData(ByteBuf data);

	public abstract Type getType();
}
