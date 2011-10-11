/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
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
