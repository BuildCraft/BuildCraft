/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import buildcraft.BuildCraftEnergy;

public class OilBucketHandler {

	@ForgeSubscribe
	public void onBucketFill(FillBucketEvent event) {

		ItemStack result = fillCustomBucket(event.world, event.target);

		if (result == null)
			return;

		event.result = result;
		event.setResult(Result.ALLOW);
	}

	public ItemStack fillCustomBucket(World world, MovingObjectPosition pos) {

		int blockID = world.getBlockId(pos.blockX, pos.blockY, pos.blockZ);

		if ((blockID == BuildCraftEnergy.oilStill.blockID || blockID == BuildCraftEnergy.oilMoving.blockID)
				&& world.getBlockMetadata(pos.blockX, pos.blockY, pos.blockZ) == 0) {

			world.setBlockWithNotify(pos.blockX, pos.blockY, pos.blockZ, 0);

			return new ItemStack(BuildCraftEnergy.bucketOil);
		} else
			return null;

	}

}
