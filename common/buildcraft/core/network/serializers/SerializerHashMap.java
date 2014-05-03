/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network.serializers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.netty.buffer.ByteBuf;

public class SerializerHashMap extends ClassSerializer {

	private static SerializerObject anonymousSerializer = new SerializerObject();

	@Override
	public void write(ByteBuf data, Object o, SerializationContext context)
			throws IllegalArgumentException, IllegalAccessException {

		HashMap map = (HashMap) o;

		if (o == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			data.writeShort(map.size());

			Set<Map.Entry> s = map.entrySet();

			for (Map.Entry e : s) {
				anonymousSerializer.write(data, e.getKey(), context);
				anonymousSerializer.write(data, e.getValue(), context);
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

			HashMap map = new HashMap ();

			for (int i = 0; i < size; ++i) {
				Object key = anonymousSerializer.read(data, null, context);
				Object value = anonymousSerializer.read(data, null, context);

				map.put(key, value);
			}

			return map;
		}
	}

}
