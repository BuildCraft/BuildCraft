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
import net.minecraft.util.BlockPos;
import buildcraft.api.core.ISerializable;
import buildcraft.core.utils.Utils;

public class PacketUpdate extends BuildCraftPacket {

	public BlockPos pos;
	public ByteBuf stream;
	public ISerializable payload;

	private int packetId;

	public PacketUpdate() {
	}

	public PacketUpdate(int packetId, ISerializable payload) {
		this(packetId, BlockPos.ORIGIN, payload);
	}

	public PacketUpdate(int packetId, BlockPos pos, ISerializable payload) {
		this(packetId);

		this.pos = pos;

		this.payload = payload;
	}

	public PacketUpdate(int packetId) {
		this.packetId = packetId;
		this.isChunkDataPacket = true;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(packetId);
		Utils.writeBlockPos(data, pos);

		if (payload != null) {
			payload.writeData(data);
		}
	}

	@Override
	public void readData(ByteBuf data) {
		packetId = data.readByte();
		pos = Utils.readBlockPos(data);

		stream = data; // for further reading
	}

	@Override
	public int getID() {
		return packetId;
	}
}
