package buildcraft.core.network;

import buildcraft.core.DefaultProps;
import io.netty.buffer.ByteBuf;

public abstract class BuildCraftPacket {

	protected boolean isChunkDataPacket = false;

	public abstract int getID();

	public abstract void readData(ByteBuf data);

	public abstract void writeData(ByteBuf data);
}
