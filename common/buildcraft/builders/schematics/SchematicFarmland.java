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

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicFarmland extends SchematicBlock {

	@Override
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(Blocks.dirt));
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z) {
		context.world().setBlock(x, y, z, block, 0, 3);
	}

	@Override
	public boolean isValid(IBuilderContext context, int x, int y, int z) {
		Block block = context.world().getBlock(x, y, z);

		return block == Blocks.farmland;
	}
}
