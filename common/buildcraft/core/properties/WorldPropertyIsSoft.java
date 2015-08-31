/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.properties;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

import buildcraft.api.core.BuildCraftAPI;

public class WorldPropertyIsSoft extends WorldProperty {
	@Override
	public boolean get(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z) {
		return block == null
				|| block.isAir(blockAccess, x, y, z)
				|| BuildCraftAPI.softBlocks.contains(block)
				|| block.isReplaceable(blockAccess, x, y, z);
	}
}
