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

import net.minecraft.block.BlockPistonBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import buildcraft.api.blueprints.IBuilderContext;

public class SchematicPiston extends SchematicRotate {

	public SchematicPiston() {
		super(BlockPistonBase.FACING);
	}

	@Override
	public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
		int localMeta = getMetaData() & 7;

		context.world().setBlockState(pos, state.withProperty(BlockPistonBase.FACING, EnumFacing.getFront(localMeta)), 1);
	}

}
