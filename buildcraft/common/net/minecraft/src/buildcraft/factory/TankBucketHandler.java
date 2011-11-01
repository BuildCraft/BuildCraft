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
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.API;
import net.minecraft.src.forge.IBucketHandler;

public class TankBucketHandler implements IBucketHandler {

	@Override
	public ItemStack fillCustomBucket(World w, int i, int j, int k) {
		if (w.getBlockId(i, j, k) == BuildCraftFactory.tankBlock.blockID) {

			TileTank tank = (TileTank) w.getBlockTileEntity(i, j, k);

			int qty = tank.empty(API.BUCKET_VOLUME, false);

			int filledBucket = API.getBucketForLiquid(tank
					.getLiquidId());

			if (qty >= API.BUCKET_VOLUME && filledBucket > 0) {
				tank.empty(API.BUCKET_VOLUME, true);

				return new ItemStack(Item.itemsList[filledBucket], 1);
			}
		
		}
		return null;
	}

}
