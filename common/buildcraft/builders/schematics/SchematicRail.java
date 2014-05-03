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

public class SchematicRail extends SchematicBlock {

	@Override
	public void rotateLeft(IBuilderContext context) {
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
			meta = 7;
			break;
		case 7:
			meta = 8;
			break;
		case 8:
			meta = 9;
			break;
		case 9:
			meta = 6;
			break;
		}
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		context.world().setBlock(x, y, z, block, 0, 3);
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		return block == context.world().getBlock(x, y, z);
	}

	@Override
	public void postProcessing(IBuilderContext context, int x, int y, int z) {
		context.world().setBlockMetadataWithNotify(x, y, z, meta, 3);
	}
}
