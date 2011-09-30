/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.forge.IBucketHandler;

public class OilBucketHandler implements IBucketHandler {

	@Override
	public ItemStack fillCustomBucket(World w, int i, int j, int k) {
		if ((w.getBlockId(i, j, k) == BuildCraftEnergy.oilStill.blockID || w
				.getBlockId(i, j, k) == BuildCraftEnergy.oilMoving.blockID)
				&& w.getBlockMetadata(i, j, k) == 0) {
			
			w.setBlockWithNotify(i, j, k, 0);
			
			return new ItemStack(BuildCraftEnergy.bucketOil);
		} else {
			return null;
		}
	}

}
