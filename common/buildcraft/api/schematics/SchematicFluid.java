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

import net.minecraft.item.ItemStack;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicFluid extends SchematicBlock {

	private final ItemStack bucketStack;

	public SchematicFluid(ItemStack bucketStack) {
		this.bucketStack = bucketStack;
	}

	@Override
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {
		if (meta == 0) {
			requirements.add(bucketStack.copy());
		}
	}

	@Override
	public boolean isValid(IBuilderContext context, int x, int y, int z) {
		if (meta == 0) {
			return block == context.world().getBlock(x, y, z) && context.world().getBlockMetadata(x, y, z) == 0;
		} else {
			return true;
		}
	}

	@Override
	public void rotateLeft(IBuilderContext context) {

	}

	@Override
	public boolean ignoreBuilding() {
		return meta != 0;
	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z) {
		if (meta == 0) {
			context.world().setBlock(x, y, z, block, 0,1);
		}
	}

}
