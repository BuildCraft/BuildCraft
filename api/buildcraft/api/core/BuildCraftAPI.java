/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.core;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public final class BuildCraftAPI {

	public static ICoreProxy proxy;

	public static final Set<Block> softBlocks = new HashSet<Block>();

	public static IWorldProperty isSoftProperty;
	public static IWorldProperty isWoodProperty;
	public static IWorldProperty isLeavesProperty;
	public static IWorldProperty[] isOreProperty;
	public static IWorldProperty isHarvestableProperty;
	public static IWorldProperty isFarmlandProperty;
	public static IWorldProperty isDirtProperty;
	public static IWorldProperty isShoveled;
	public static IWorldProperty isFluidSource;

	/**
	 * Deactivate constructor
	 */
	private BuildCraftAPI() {
	}

	public static boolean isSoftBlock(World world, int x, int y, int z) {
		return isSoftProperty.get(world, x, y, z);
	}
}
