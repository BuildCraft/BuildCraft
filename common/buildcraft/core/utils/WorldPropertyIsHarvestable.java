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
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockMelon;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WorldPropertyIsHarvestable extends WorldProperty {

	@Override
	public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
		Block block = state.getBlock();
		if (block instanceof BlockFlower
				|| block instanceof BlockTallGrass
				|| block instanceof BlockMelon
				|| block instanceof BlockMushroom
				|| block instanceof BlockDoublePlant) {
			return true;
		} else if (block instanceof BlockCactus || block instanceof BlockReed) {
			if (pos.getY() > 0 && blockAccess.getBlockState(pos.down()).getBlock() == block) {
				return true;
			}
		} else if (block instanceof BlockCrops) {
			return ((Integer)state.getValue(BlockCrops.AGE)).intValue() == 7;
		}

		return false;
	}
}
