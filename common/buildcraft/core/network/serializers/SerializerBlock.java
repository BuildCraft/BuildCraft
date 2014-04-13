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
import net.minecraft.block.Block;

public class SerializerBlock extends ClassSerializer {

	@Override
	public void write (ByteBuf data, Object o, SerializationContext context) {
		Block b = (Block) o;

		if (b == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			data.writeInt(Block.getIdFromBlock(b));
		}
	}

	@Override
	public Object read (ByteBuf data, Object o, SerializationContext context) {
		if (!data.readBoolean()) {
			return null;
		} else {
			return Block.getBlockById(data.readInt());
		}
	}
}
