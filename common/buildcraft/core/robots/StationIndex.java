/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.EnumFacing;

import net.minecraft.util.BlockPos;
import buildcraft.api.robots.IDockingStation;
import buildcraft.core.utils.Utils;

public class StationIndex {

	public BlockPos index;
	public EnumFacing side = null;

	protected StationIndex() {
	}

	public StationIndex(EnumFacing iSide, BlockPos pos) {
		side = iSide;
		index = pos;
	}

	public StationIndex(IDockingStation station) {
		side = station.side();
		index = station.pos();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != getClass()) {
			return false;
		}

		StationIndex compareId = (StationIndex) obj;

		return index.equals(compareId.index)
				&& side == compareId.side;
	}

	@Override
	public int hashCode() {
		return (index.hashCode() * 37) + side.ordinal();
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound indexNBT = new NBTTagCompound();
		Utils.writeBlockPos(indexNBT, index);
		nbt.setTag("index", indexNBT);
		nbt.setByte("side", (byte) side.ordinal());
	}

	protected void readFromNBT(NBTTagCompound nbt) {
		index = Utils.readBlockPos(nbt.getCompoundTag("index"));
		side = EnumFacing.values()[nbt.getByte("side")];
	}
}
