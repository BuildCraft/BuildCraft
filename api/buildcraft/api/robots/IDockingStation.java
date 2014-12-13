/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.robots;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;

public interface IDockingStation {

	int x();

	int y();

	int z();

	ForgeDirection side();

	EntityRobotBase robotTaking();

	long robotIdTaking();

	long linkedId();

	boolean isTaken();

	void writeToNBT(NBTTagCompound nbt);

	void readFromNBT(NBTTagCompound nbt);

	BlockIndex index();

	boolean take(EntityRobotBase robot);
}
