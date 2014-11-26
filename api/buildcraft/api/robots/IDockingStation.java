/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.BlockIndex;

public interface IDockingStation {

	BlockPos pos();

	EnumFacing side();

	EntityRobotBase robotTaking();

	long robotIdTaking();

	long linkedId();

	boolean isTaken();

	void writeToNBT(NBTTagCompound nbt);

	void readFromNBT(NBTTagCompound nbt);

	boolean take(EntityRobotBase robot);
}
