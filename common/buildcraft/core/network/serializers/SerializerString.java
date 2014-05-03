/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network.serializers;

import io.netty.buffer.ByteBuf;

import buildcraft.core.utils.Utils;

public class SerializerString extends ClassSerializer {

	@Override
	public void write (ByteBuf data, Object o, SerializationContext context) {
		String s = (String) o;

		if (s == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			Utils.writeUTF(data, s);
		}
	}

	@Override
	public Object read (ByteBuf data, Object o, SerializationContext context) {
		if (!data.readBoolean()) {
			return null;
		} else {
			return Utils.readUTF(data);
		}
	}
}
