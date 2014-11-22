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
import buildcraft.api.core.ISerializable;

public class PacketWrapper implements ISerializable {
	private final ISerializable wrapped;

	public PacketWrapper(ISerializable wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void readData(ByteBuf stream) {
		wrapped.readData(stream);
	}

	@Override
	public void writeData(ByteBuf stream) {
		wrapped.writeData(stream);
	}
}
