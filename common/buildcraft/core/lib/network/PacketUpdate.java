/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import io.netty.buffer.ByteBuf;
import buildcraft.api.core.ISerializable;

public class PacketUpdate extends Packet {

	public int posX;
	public int posY;
	public int posZ;
	public ByteBuf stream;
	public ISerializable payload;

	private int packetId;

	public PacketUpdate() {
	}

	public PacketUpdate(int packetId, ISerializable payload) {
		this(packetId, 0, 0, 0, payload);
	}

	public PacketUpdate(int packetId, int posX, int posY, int posZ, ISerializable payload) {
		this(packetId);

		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;

		this.payload = payload;
	}

	public PacketUpdate(int packetId) {
		this.packetId = packetId;
		this.isChunkDataPacket = true;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(packetId);
		data.writeInt(posX);
		data.writeShort(posY);
		data.writeInt(posZ);

		if (payload != null) {
			payload.writeData(data);
		}
	}

	@Override
	public void readData(ByteBuf data) {
		packetId = data.readByte();
		posX = data.readInt();
		posY = data.readShort();
		posZ = data.readInt();

		stream = data; // for further reading
	}

	@Override
	public int getID() {
		return packetId;
	}
}
