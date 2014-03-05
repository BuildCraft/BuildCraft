/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.schematics;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.IBuilderContext;

public class SchematicDirt extends Schematic {

	@Override
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(Blocks.dirt));
	}

	@Override
	public void writeToWorld(IBuilderContext context) {
		context.world().setBlock(x, y, z, Blocks.dirt, meta, 3);
	}

	@Override
	public boolean isValid(IBuilderContext context) {
		Block block = context.world().getBlock(x, y, z);

		return block == Blocks.dirt || block == Blocks.grass || block == Blocks.farmland;
	}
}
