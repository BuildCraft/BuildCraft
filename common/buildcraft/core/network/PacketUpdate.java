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

public class PacketUpdate extends BuildCraftPacket {

	public int posX;
	public int posY;
	public int posZ;
	public PacketPayload payload;

	private int packetId;

	public PacketUpdate() {
	}

	public PacketUpdate(int packetId, PacketPayload payload) {
		this(packetId, 0, 0, 0, payload);
	}

	public PacketUpdate(int packetId, int posX, int posY, int posZ, PacketPayload payload) {
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
		data.writeInt(posY);
		data.writeInt(posZ);

		if (payload != null) {
			payload.writeData(data);
		} else {
			data.writeByte(0);
		}
	}

	@Override
	public void readData(ByteBuf data) {
		packetId = data.readByte();
		posX = data.readInt();
		posY = data.readInt();
		posZ = data.readInt();

		payload = new PacketPayload();

		if (payload != null) {
			payload.readData(data);
		}
	}

	@Override
	public int getID() {
		return packetId;
	}
}
