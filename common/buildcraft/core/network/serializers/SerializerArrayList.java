/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network.serializers;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;

public class SerializerArrayList extends ClassSerializer {

	private static SerializerObject anonymousSerializer = new SerializerObject();

	@Override
	public void write(ByteBuf data, Object o, SerializationContext context)
			throws IllegalArgumentException, IllegalAccessException {

		ArrayList list = (ArrayList) o;

		if (o == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			data.writeShort(list.size());

			for (Object val : list) {
				anonymousSerializer.write(data, val, context);
			}
		}
	}

	@Override
	public Object read(ByteBuf data, Object o, SerializationContext context)
			throws IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException {

		if (!data.readBoolean()) {
			return null;
		} else {
			int size = data.readShort();

			ArrayList list = new ArrayList ();

			for (int i = 0; i < size; ++i) {
				Object val = anonymousSerializer.read(data, null, context);

				list.add(val);
			}

			return list;
		}
	}

}
