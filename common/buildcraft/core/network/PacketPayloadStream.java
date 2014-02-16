/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Alternative Packet Payload system.
 *
 * Note, you cannot use a Stream payload and the TileNetworkData annotation at
 * the same time. Attempting to do will most likely result in a class cast
 * exception somewhere.
 */
public class PacketPayloadStream extends PacketPayload {

	public static interface StreamWriter {

		public void writeData(ByteBuf data);
	}
	private StreamWriter handler;
	public ByteBuf stream;

	public PacketPayloadStream() {
	}

	public PacketPayloadStream(StreamWriter handler) {
		this.handler = handler;
	}

	@Override
	public void writeData(ByteBuf data) {
		handler.writeData(data);
	}

	@Override
	public void readData(ByteBuf data) {
		stream = data;
	}
}
