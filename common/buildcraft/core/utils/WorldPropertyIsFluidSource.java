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
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.fluids.BlockFluidBase;

public class WorldPropertyIsFluidSource extends WorldProperty {

	@Override
	public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
		return (state.getBlock() instanceof BlockLiquid || state.getBlock() instanceof BlockFluidBase)
				&& ((Integer) state.getValue(BlockFluidBase.LEVEL)).intValue() == 0;
	}
}
