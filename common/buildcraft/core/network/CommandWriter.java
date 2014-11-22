package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

public abstract class CommandWriter {
	public abstract void write(ByteBuf data);
}
