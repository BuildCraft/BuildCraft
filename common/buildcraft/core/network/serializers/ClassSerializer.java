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

public abstract class ClassSerializer {

	public Class<? extends Object> mappedClass;

	public abstract void write(ByteBuf data, Object o,
			SerializationContext context) throws IllegalArgumentException,
			IllegalAccessException;

	public abstract Object read(ByteBuf data, Object o,
			SerializationContext context) throws IllegalArgumentException,
			IllegalAccessException, InstantiationException,
			ClassNotFoundException;

}
