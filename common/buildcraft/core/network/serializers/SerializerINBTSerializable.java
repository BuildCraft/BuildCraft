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

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.core.network.INBTSerializable;

public class SerializerINBTSerializable extends SerializerNBT {
	@Override
	public void write (ByteBuf data, Object o, SerializationContext context) {
		super.write(data, ((INBTSerializable) o).serializeNBT(), context);
	}

	@Override
	public Object read (ByteBuf data, Object o, SerializationContext context) {
		((INBTSerializable) o).serializeNBT((NBTTagCompound) super.read(data, o, context));
		return o;
	}
}
