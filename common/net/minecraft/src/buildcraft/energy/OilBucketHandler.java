/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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
