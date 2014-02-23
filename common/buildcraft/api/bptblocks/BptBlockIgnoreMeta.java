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

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;

public class BptBlockIgnoreMeta extends BptBlock {

	public BptBlockIgnoreMeta(Block block) {
		super(block);
	}

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList<ItemStack> requirements) {
		requirements.add(new ItemStack(slot.block, 1, 0));
	}

	@Override
	public boolean isValid(BptSlotInfo slot, IBptContext context) {
		return slot.block == context.world().getBlock(slot.x, slot.y, slot.z);
	}
}
