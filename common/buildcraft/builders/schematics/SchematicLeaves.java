/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import java.util.LinkedList;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicLeaves extends SchematicBlock {
	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		return block == context.world().getBlock(x, y, z) && (meta & 3) == (context.world().getBlockMetadata(x, y, z) & 3);
	}
}
