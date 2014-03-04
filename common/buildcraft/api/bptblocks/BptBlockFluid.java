/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.bptblocks;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.IBptContext;

public class BptBlockFluid extends BptBlock {

	private final ItemStack bucketStack;

	public BptBlockFluid(ItemStack bucketStack) {
		this.bucketStack = bucketStack;
	}

	@Override
	public void addRequirements(IBptContext context, LinkedList<ItemStack> requirements) {
		if (meta == 0) {
			requirements.add(bucketStack.copy());
		}
	}

	@Override
	public boolean isValid(IBptContext context) {
		if (meta == 0) {
			return block == context.world().getBlock(x, y, z) && context.world().getBlockMetadata(x, y, z) == 0;
		} else {
			return true;
		}
	}

	@Override
	public void rotateLeft(IBptContext context) {

	}

	@Override
	public boolean ignoreBuilding() {
		return meta != 0;
	}

	@Override
	public void buildBlock(IBptContext context) {
		if (meta == 0) {
			context.world().setBlock(x, y, z, block, 0,1);
		}
	}

}
