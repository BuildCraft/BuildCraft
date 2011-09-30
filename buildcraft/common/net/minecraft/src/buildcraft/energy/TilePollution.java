/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.core.BlockIndex;

public class TilePollution extends TileEntity {

	public boolean init = false;
	public SafeTimeTracker timeTracker = new SafeTimeTracker();
	public int saturation = 0; /* from 0 to 100 */	
	
	public void updateEntity () {
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
//				int remaining = BuildCraftEnergy.createPollution(worldObj,
//						xCoord, yCoord, zCoord, saturation);
//
//				saturation = remaining;
//
//				if (remaining == 0) {
//					worldObj.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
//				} else {
//					worldObj.setBlockMetadata(xCoord, yCoord, zCoord,
//							saturation * 16 / 100);
//					worldObj.markBlockNeedsUpdate(zCoord, yCoord, zCoord);
//				}
			}
		}
		
		
	}
	
}
