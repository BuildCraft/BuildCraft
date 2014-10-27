/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network.serializers;

import java.util.BitSet;
import io.netty.buffer.ByteBuf;

import buildcraft.core.utils.BitSetUtils;

public class SerializerBitSet extends ClassSerializer {

	@Override
	public void write (ByteBuf data, Object o, SerializationContext context) {
		if (o == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);

			BitSet set = (BitSet) o;
			byte[] bytes = BitSetUtils.toByteArray(set);
			data.writeInt(bytes.length);
			data.writeBytes(bytes);
		}
	}

	@Override
	public Object read (ByteBuf data, Object o, SerializationContext context) {
		if (!data.readBoolean()) {
			return null;
		}

		int actualSize = data.readInt();
		byte[] bytes = new byte[actualSize];
		data.readBytes(bytes);

		BitSet set = BitSetUtils.fromByteArray(bytes);

		return set;
	}
}
