/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.bptblocks;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import buildcraft.api.blueprints.BlueprintManager;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;

@Deprecated
public class BptBlockDelegate extends BptBlock {

	final int delegateTo;

	public BptBlockDelegate(int blockId, int delegateTo) {
		super(blockId);

		this.delegateTo = delegateTo;
	}

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList<ItemStack> requirements) {
		BptSlotInfo newSlot = slot.clone();
		slot.blockId = delegateTo;

		if (BlueprintManager.blockBptProps[delegateTo] != null) {
			BlueprintManager.blockBptProps[delegateTo].addRequirements(newSlot, context, requirements);
		} else {
			super.addRequirements(newSlot, context, requirements);
		}
	}

	@Override
	public boolean isValid(BptSlotInfo slot, IBptContext context) {
		BptSlotInfo newSlot = slot.clone();
		slot.blockId = delegateTo;

		if (BlueprintManager.blockBptProps[delegateTo] != null)
			return BlueprintManager.blockBptProps[delegateTo].isValid(newSlot, context);
		else
			return super.isValid(newSlot, context);
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		BptSlotInfo newSlot = slot.clone();
		slot.blockId = delegateTo;

		if (BlueprintManager.blockBptProps[delegateTo] != null) {
			BlueprintManager.blockBptProps[delegateTo].rotateLeft(newSlot, context);
		} else {
			super.rotateLeft(newSlot, context);
		}
	}

}
