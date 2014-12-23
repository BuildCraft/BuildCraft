/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import buildcraft.core.utils.Utils;

public class PacketSlotChange extends PacketCoordinates {

	public int slot;
	public ItemStack stack;

	public PacketSlotChange() {
	}

	public PacketSlotChange(int id, BlockPos pos, int slot, ItemStack stack) {
		super(id, pos);
		this.slot = slot;
		this.stack = stack;
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		data.writeShort(slot);
		Utils.writeStack(data, stack);		
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		this.slot = data.readUnsignedShort();
		stack = Utils.readStack(data);
	}
}
