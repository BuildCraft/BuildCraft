/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.NetworkData;

public class ChunkIndex {

	@NetworkData
	public int x, z;

	public ChunkIndex() {

	}

	public ChunkIndex(int iX, int iZ) {
		x = iX;
		z = iZ;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ChunkIndex) {
			ChunkIndex c = (ChunkIndex) obj;

			return c.x == x && c.z == z;
		}

		return super.equals(obj);
	}


	@Override
	public int hashCode() {
		return (x * 37 + z);
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("x", x);
		nbt.setInteger("z", z);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		x = nbt.getInteger("x");
		z = nbt.getInteger("z");
	}
}
