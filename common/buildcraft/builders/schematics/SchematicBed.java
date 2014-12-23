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

import net.minecraft.block.BlockBed;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.core.BlockBuildCraft;

public class SchematicBed extends SchematicBlock {

	@Override
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
		if ((getMetaData() & 8) == 0) {
			requirements.add(new ItemStack(Items.bed));
		}
	}

	@Override
	public void storeRequirements(IBuilderContext context, BlockPos pos) {
		// cancel requirements reading
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		int orientation = getMetaData() & 7;
		int others = getMetaData() - orientation;

		switch (orientation) {
		case 0:
			setMetaData(1 + others);
			break;
		case 1:
			setMetaData(2 + others);
			break;
		case 2:
			setMetaData(3 + others);
			break;
		case 3:
			setMetaData(0 + others);
			break;
		}
	}

	@Override
	public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
		if ((getMetaData() & 8) != 0) {
			return;
		}
		
		context.world().setBlockState(pos, state.withProperty(BlockBed.PART, getFace()), 3);
		
		int x2 = pos.getX();
		int z2 = pos.getY();

		switch (getMetaData()) {
		case 0:
			z2++;
			break;
		case 1:
			x2--;
			break;
		case 2:
			z2--;
			break;
		case 3:
			x2++;
			break;
		}

		context.world().setBlockState(new BlockPos(x2, pos.getY(), z2), state.withProperty(BlockBed.PART, EnumFacing.getFront(getMetaData() + 8)), 3);
	}

	@Override
	public boolean doNotBuild() {
		return (getMetaData() & 8) != 0;
	}
}
