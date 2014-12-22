/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.core.BlockBuildCraft;

public class SchematicRail extends SchematicBlock {

	@Override
	public void rotateLeft(IBuilderContext context) {
		int meta = getMetaData();
		switch (meta) {
		case 0:
			meta = 1;
			break;
		case 1:
			meta = 0;
			break;

		case 2:
			meta = 5;
			break;
		case 3:
			meta = 4;
			break;
		case 4:
			meta = 2;
			break;
		case 5:
			meta = 3;
			break;

		case 6:
		case 7:
		case 8:
			meta++;
			break;
		case 9:
			meta = 6;
			break;
		}
		setMetaData(meta);
	}

	@Override
	public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
		context.world().setBlockState(pos, state.withProperty(BlockRail.SHAPE, EnumRailDirection.NORTH_SOUTH), 3);
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
		return state.getBlock() == context.world().getBlockState(pos).getBlock();
	}

	@Override
	public void postProcessing(IBuilderContext context, BlockPos pos) {
		context.world().setBlockState(pos, state.withProperty(BlockRail.SHAPE, EnumRailDirection.byMetadata(getMetaData())), 3);
	}
	
	@Override
	public void setMetaData(int newValue)
	{
		state = state.withProperty(BlockRail.SHAPE, EnumRailDirection.byMetadata(newValue));
	}
}
