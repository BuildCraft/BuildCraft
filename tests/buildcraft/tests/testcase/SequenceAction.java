/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests.testcase;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.core.NetworkData;

public class SequenceAction {

	@NetworkData
	public long date;

	@NetworkData
	public World world;

	public void execute() {

	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("date", date);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		date = nbt.getLong("date");
	}

}
