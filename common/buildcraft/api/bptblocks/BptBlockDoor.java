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

public class BptBlockDoor extends BptBlock {

	final ItemStack stack;

	public BptBlockDoor(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public void addRequirements(IBptContext context, LinkedList<ItemStack> requirements) {
		if ((meta & 8) == 0) {
			requirements.add(stack.copy());
		}
	}

	@Override
	public void rotateLeft(IBptContext context) {
		int orientation = (meta & 3);
		int others = meta - orientation;

		switch (orientation) {
		case 0:
			meta = 1 + others;
			break;
		case 1:
			meta = 2 + others;
			break;
		case 2:
			meta = 3 + others;
			break;
		case 3:
			meta = 0 + others;
			break;
		}
	}

	@Override
	public boolean ignoreBuilding() {
		return (meta & 8) != 0;
	}

	@Override
	public void buildBlock(IBptContext context) {
		context.world().setBlock(x, y, z, block, meta, 3);
		context.world().setBlock(x, y + 1, z, block, meta + 8, 3);

		context.world().setBlockMetadataWithNotify(x, y + 1, z, meta + 8, 3);
		context.world().setBlockMetadataWithNotify(x, y, z, meta, 3);

	}
}
