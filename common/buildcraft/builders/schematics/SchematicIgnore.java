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

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicIgnore extends SchematicBlock {

	@Override
	public void writeRequirementsToWorld(IBuilderContext context, LinkedList<ItemStack> requirements) {

	}

	@Override
	public void rotateLeft(IBuilderContext context) {

	}

	@Override
	public void writeToBlueprint(IBuilderContext context, int x, int y, int z) {

	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {

	}

	@Override
	public void writeRequirementsToBlueprint(IBuilderContext context, int x, int y, int z) {

	}

	@Override
	public boolean doNotBuild() {
		return true;
	}

}
