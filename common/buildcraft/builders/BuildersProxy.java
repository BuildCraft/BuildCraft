/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class BuildersProxy {

	public static boolean canPlaceTorch(World world, int i, int j, int k) {
		Block block = Block.blocksList[world.getBlockId(i, j, k)];

		if (block == null || !block.renderAsNormalBlock())
			return false;
		else
			return true;
	}

	public static String getOwner(TileBlueprintLibrary library) {
		return library.owner;
	}

}
