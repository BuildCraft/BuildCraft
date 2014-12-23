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
import buildcraft.core.utils.Utils;

public abstract class PacketCoordinates extends BuildCraftPacket {

	public BlockPos pos;

	private int id;

	public PacketCoordinates() {
	}

	public PacketCoordinates(int id, BlockPos pos) {
		this.id = id;
		this.pos = pos;
	}

	@Override
	public void writeData(ByteBuf data) {
		data.writeByte(id);
		Utils.writeBlockPos(data, pos);
	}

	@Override
	public void readData(ByteBuf data) {
		id = data.readByte ();
		pos = Utils.readBlockPos(data);
	}

	@Override
	public int getID() {
		return id;
	}
}
