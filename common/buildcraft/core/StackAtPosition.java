/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.ISerializable;
import buildcraft.api.core.Position;
import buildcraft.core.utils.NetworkUtils;
import buildcraft.core.utils.Utils;

public class StackAtPosition implements ISerializable {
	public ItemStack stack;
	public Position pos;
	public boolean display;

	@Override
	public void readData(ByteBuf stream) {
		stack = NetworkUtils.readStack(stream);
	}

	@Override
	public void writeData(ByteBuf stream) {
		NetworkUtils.writeStack(stream, stack);
	}
}