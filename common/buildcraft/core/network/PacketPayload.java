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

public class PacketPayload {

	public ByteBuf stream;
	private StreamWriter handler;

	public interface StreamWriter {
		void writeData(ByteBuf data);
	}

	public PacketPayload() {
	}

	public PacketPayload(StreamWriter handler) {
		this.handler = handler;
	}

	public void writeData(ByteBuf data) {
		handler.writeData(data);
	}

	public void readData(ByteBuf data) {
		stream = data;
	}
}
