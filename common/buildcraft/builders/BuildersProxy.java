/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public final class BuildersProxy {

	/**
	 * Forbid contruction of this class
	 */
	private BuildersProxy() {
	}

	public static boolean canPlaceTorch(World world, int i, int j, int k) {
		Block block = world.getBlock(i, j, k);

		return !(block == null || !block.renderAsNormalBlock());
	}
}
