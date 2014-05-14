/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory.schematics;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicTank extends SchematicTile {

	@Override
	public void writeRequirementsToWorld(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(block));
	}

	@Override
	public void writeRequirementsToBlueprint(IBuilderContext context, int x, int y, int z) {

	}

	@Override
	public void writeToBlueprint(IBuilderContext context, int x, int y, int z) {

	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		context.world().setBlock(x, y, z, block, meta, 3);
	}

	@Override
	public BuildingStage getBuildStage() {
		return BuildingStage.STANDALONE;
	}
}
