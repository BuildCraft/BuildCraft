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
import buildcraft.api.blueprints.BlueprintManager;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;

public class BptBlockDelegate extends BptBlock {

	final Block delegateTo;
	BptBlock delegated;

	public BptBlockDelegate(Block block, Block delegateTo) {
		super(block);

		this.delegateTo = delegateTo;
	}

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList<ItemStack> requirements) {
		BptSlotInfo newSlot = slot.clone();
		slot.block = delegateTo;

		getDelegated ().addRequirements(newSlot, context, requirements);
	}

	@Override
	public boolean isValid(BptSlotInfo slot, IBptContext context) {
		BptSlotInfo newSlot = slot.clone();
		slot.block = delegateTo;

		return getDelegated ().isValid(newSlot, context);
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		BptSlotInfo newSlot = slot.clone();
		slot.block = delegateTo;

		getDelegated().rotateLeft(newSlot, context);
	}

	private BptBlock getDelegated () {
		if (delegated == null) {
			delegated = BlueprintManager.getBptBlock(delegateTo);
		}

		return delegated;
	}

}
