/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.core.NetworkData;

public class BlueprintReadConfiguration {

	@NetworkData
	public boolean rotate = true;

	@NetworkData
	public boolean readTiles = true;

	@NetworkData
	public boolean excavate = true;

	@NetworkData
	public boolean explicitOnly = false;

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("rotate", rotate);
		nbttagcompound.setBoolean("readAllBlocks", readTiles);
		nbttagcompound.setBoolean("excavate", excavate);
		nbttagcompound.setBoolean("explicitOnly", explicitOnly);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		rotate = nbttagcompound.getBoolean("rotate");
		readTiles = nbttagcompound.getBoolean("readAllBlocks");
		excavate = nbttagcompound.getBoolean("excavate");
		explicitOnly = nbttagcompound.getBoolean("explicitOnly");
	}

}
