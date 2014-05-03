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

public class SerializerObject extends ClassSerializer {

	@Override
	public void write(ByteBuf data, Object o, SerializationContext context)
			throws IllegalArgumentException, IllegalAccessException {

		if (o == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			Class realClass = o.getClass();

			ClassSerializer delegateMapping;

			if (context.classToId.containsKey(realClass.getCanonicalName())) {
				int index = context.classToId.get(realClass.getCanonicalName()) + 1;
				data.writeByte(index);
				delegateMapping = context.idToClass.get(index - 1);
			} else {
				int index = context.classToId.size() + 1;
				delegateMapping = ClassMapping.get(realClass);
				data.writeByte(index);
				Utils.writeUTF(data, realClass.getCanonicalName());
				context.classToId.put(realClass.getCanonicalName(),
						context.classToId.size());
				context.idToClass.add(delegateMapping);
			}

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
			int index = data.readByte();

			ClassSerializer delegateMapping;

			if (context.idToClass.size() < index) {
				String className = Utils.readUTF(data);

				Class cls = Class.forName(className);
				delegateMapping = ClassMapping.get(cls);

				context.idToClass.add(ClassMapping.get(cls));
			} else {
				delegateMapping = context.idToClass.get(index - 1);
			}

			if (delegateMapping instanceof ClassMapping) {
				return ((ClassMapping) delegateMapping).readClass(o, data, context);
			} else {
				return delegateMapping.read(data, o, context);
			}
		}
	}

}
