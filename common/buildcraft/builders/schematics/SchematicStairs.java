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

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicStairs extends SchematicBlock {

	@Override
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(state.getBlock(), 1, 0));
	}

	@Override
	public void storeRequirements(IBuilderContext context, BlockPos pos) {

	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
		return state.getBlock() == context.world().getBlockState(pos).getBlock();
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		int pos = getMetaData() & 3;
		int others = getMetaData() - pos;

		switch (pos) {
		case 0:
			pos = 2;
			break;
		case 1:
			pos = 3;
			break;
		case 2:
			pos = 1;
			break;
		case 3:
			pos = 0;
			break;
		}

		setMetaData(pos + others);
	}

	@Override
	public BuildingStage getBuildStage() {
		return BuildingStage.STANDALONE;
	}
}
