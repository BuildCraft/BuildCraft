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
