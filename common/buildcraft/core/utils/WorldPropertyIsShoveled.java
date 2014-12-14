/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockClay;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WorldPropertyIsShoveled extends WorldProperty {

	@Override
	public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
		return state.getBlock() instanceof BlockDirt
				|| state.getBlock() instanceof BlockSand
				|| state.getBlock() instanceof BlockClay
				|| state.getBlock() instanceof BlockGravel
				|| state.getBlock() instanceof BlockFarmland
				|| state.getBlock() instanceof BlockGrass
				|| state.getBlock() instanceof BlockSnow;
	}
}
