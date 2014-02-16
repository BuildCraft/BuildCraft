/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import buildcraft.core.DefaultProps;
import io.netty.buffer.ByteBuf;

public abstract class BuildCraftPacket {

	protected boolean isChunkDataPacket = false;

	public abstract int getID();

	public abstract void readData(ByteBuf data);

	public abstract void writeData(ByteBuf data);
}
