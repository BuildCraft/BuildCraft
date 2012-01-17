/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.forge.IBucketHandler;

public class TankBucketHandler implements IBucketHandler {

	@Override
	public ItemStack fillCustomBucket(World w, int i, int j, int k) {
		if (w.getBlockId(i, j, k) == BuildCraftFactory.tankBlock.blockID) {

			TileTank tank = (TileTank) w.getBlockTileEntity(i, j, k);

			int qty = tank.empty(BuildCraftAPI.BUCKET_VOLUME, false);

			ItemStack filledBucket = BuildCraftAPI.getFilledItemForLiquid(tank
					.getLiquidId());

			if (qty >= BuildCraftAPI.BUCKET_VOLUME && filledBucket != null) {
				tank.empty(BuildCraftAPI.BUCKET_VOLUME, true);

				return filledBucket;
			}
		
		}
		return null;
	}

}
