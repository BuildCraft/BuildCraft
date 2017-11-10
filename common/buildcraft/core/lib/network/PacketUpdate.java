/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import io.netty.buffer.ByteBuf;

import buildcraft.api.core.ISerializable;

public abstract class PacketUpdate extends Packet {
	public ByteBuf stream;
	public ISerializable payload;

	private int packetId;

	public PacketUpdate() {
	}

	public PacketUpdate(int packetId, ISerializable payload) {
		this(packetId);

		this.payload = payload;
	}

	public PacketUpdate(int packetId) {
		this.packetId = packetId;
		this.isChunkDataPacket = true;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(packetId);
		writeIdentificationData(data);

		if (payload != null) {
			payload.writeData(data);
		}
	}

	public abstract void writeIdentificationData(ByteBuf data);

	@Override
	public void readData(ByteBuf data) {
		packetId = data.readByte();
		readIdentificationData(data);

		stream = data; // for further reading
	}

	public abstract void readIdentificationData(ByteBuf data);

	@Override
	public int getID() {
		return packetId;
	}
}
