/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

public class PacketNBT extends PacketCoordinates {

	private NBTTagCompound nbttagcompound;

	public PacketNBT() {
	}

	public PacketNBT(int id, NBTTagCompound nbttagcompound, int xCoord, int yCoord, int zCoord) {
		super(id, xCoord, yCoord, zCoord);
		this.nbttagcompound = nbttagcompound;
	}

	@Override
	public void writeData(ByteBuf data) {
		super.writeData(data);

		try {
			byte[] compressed = CompressedStreamTools.compress(nbttagcompound);
			data.writeShort(compressed.length);
			data.writeBytes(compressed);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void readData(ByteBuf data) {
		super.readData(data);

		short length = data.readShort();
		byte[] compressed = new byte[length];
		data.readBytes(compressed);
		
		try {
			this.nbttagcompound = CompressedStreamTools.func_152457_a(compressed, NBTSizeTracker.field_152451_a);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public NBTTagCompound getTagCompound() {
		return this.nbttagcompound;
	}

}
