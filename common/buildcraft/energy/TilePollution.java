/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import buildcraft.BuildCraftEnergy;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.BlockIndex;
import net.minecraft.tileentity.TileEntity;

public class TilePollution extends TileEntity {

	public boolean init = false;
	public SafeTimeTracker timeTracker = new SafeTimeTracker();
	public int saturation = 0; /* from 0 to 100 */

	@Override
	public void updateEntity() {
		if (!init) {
			init = true;
			timeTracker.markTime(worldObj);
			BlockIndex index = new BlockIndex(xCoord, yCoord, zCoord);

			if (BuildCraftEnergy.saturationStored.containsKey(index)) {
				saturation = BuildCraftEnergy.saturationStored.remove(index);
			} else {
				saturation = 1;
			}
		} else {
			if (timeTracker.markTimeIfDelay(worldObj, 20)) {
				// int remaining = BuildCraftEnergy.createPollution(worldObj,
				// xCoord, yCoord, zCoord, saturation);
				//
				// saturation = remaining;
				//
				// if (remaining == 0) {
				// worldObj.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
				// } else {
				// worldObj.setBlockMetadata(xCoord, yCoord, zCoord,
				// saturation * 16 / 100);
				// worldObj.markBlockNeedsUpdate(zCoord, yCoord, zCoord);
				// }
			}
		}

	}

}
