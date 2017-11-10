/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;

import buildcraft.core.lib.utils.NetworkUtils;

public class PacketSlotChange extends PacketCoordinates {

	public int slot;
	public ItemStack stack;

	public PacketSlotChange() {
	}

	public PacketSlotChange(int id, int x, int y, int z, int slot, ItemStack stack) {
		super(id, x, y, z);
		this.slot = slot;
		this.stack = stack;
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		data.writeShort(slot);
		NetworkUtils.writeStack(data, stack);
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		this.slot = data.readUnsignedShort();
		stack = NetworkUtils.readStack(data);
	}
}
