/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Alternative Packet Payload system.
 *
 * Note, you cannot use a Stream payload and the TileNetworkData annotation at
 * the same time. Attempting to do will most likely result in a class cast
 * exception somewhere.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class PacketPayloadStream extends PacketPayload {

	public static interface StreamWriter {

		public void writeData(DataOutputStream data) throws IOException;
	}
	private StreamWriter handler;
	public DataInputStream stream;

	public PacketPayloadStream() {
	}

	public PacketPayloadStream(StreamWriter handler) {
		this.handler = handler;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		handler.writeData(data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		stream = data;
	}

	@Override
	public Type getType() {
		return Type.STREAM;
	}
}
