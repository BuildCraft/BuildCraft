/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.ISerializable;
import buildcraft.api.core.Position;
import buildcraft.core.lib.utils.NetworkUtils;

public class StackAtPosition implements ISerializable {
	public ItemStack stack;
	public Position pos;
	public boolean display;

	// Rendering only!
	public boolean generatedListId;
	public int glListId;

	@Override
	public void readData(ByteBuf stream) {
		stack = NetworkUtils.readStack(stream);
	}

	@Override
	public void writeData(ByteBuf stream) {
		NetworkUtils.writeStack(stream, stack);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StackAtPosition)) {
			return false;
		}
		StackAtPosition other = (StackAtPosition) o;
		return other.stack.equals(stack) && other.pos.equals(pos) && other.display == display;
	}

	@Override
	public int hashCode() {
		return stack.hashCode() * 17 + pos.hashCode();
	}
}