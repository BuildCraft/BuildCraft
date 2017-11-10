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

public abstract class PacketCoordinates extends Packet {

	public int posX;
	public int posY;
	public int posZ;

	private int id;

	public PacketCoordinates() {
	}

	public PacketCoordinates(int id, int x, int y, int z) {
		this.id = id;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(id);
		data.writeInt(posX);
		data.writeShort(posY);
		data.writeInt(posZ);
	}

	@Override
	public void readData(ByteBuf data) {
		id = data.readByte();
		posX = data.readInt();
		posY = data.readShort();
		posZ = data.readInt();
	}

	@Override
	public int getID() {
		return id;
	}
}
