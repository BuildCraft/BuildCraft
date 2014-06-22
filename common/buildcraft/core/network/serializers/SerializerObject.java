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

import buildcraft.core.network.NetworkIdRegistry;

public class SerializerObject extends ClassSerializer {

	@Override
	public void write(ByteBuf data, Object o, SerializationContext context)
			throws IllegalArgumentException, IllegalAccessException {
		if (o == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			Class<? extends Object> realClass = o.getClass();

			NetworkIdRegistry.write(data, realClass.getCanonicalName());
			ClassSerializer delegateMapping = ClassMapping.get(realClass);

			if (delegateMapping instanceof ClassMapping) {
				((ClassMapping) delegateMapping).writeClass(o, data, context);
			} else {
				delegateMapping.write(data, o, context);
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
			String className = NetworkIdRegistry.read(data);
			Class cls = Class.forName(className);
			ClassSerializer delegateMapping = ClassMapping.get(cls);

			if (delegateMapping instanceof ClassMapping) {
				return ((ClassMapping) delegateMapping).readClass(o, data, context);
			} else {
				return delegateMapping.read(data, o, context);
			}
		}
	}
}
