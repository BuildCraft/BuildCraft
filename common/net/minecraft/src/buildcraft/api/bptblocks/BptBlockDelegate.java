/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api.bptblocks;

import java.util.LinkedList;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.BptBlock;
import net.minecraft.src.buildcraft.api.BptSlotInfo;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.IBptContext;

public class BptBlockDelegate extends BptBlock {

	final int delegateTo;
	
	public BptBlockDelegate (int blockId, int delegateTo) {
		super (blockId);
		
		this.delegateTo = delegateTo;
	}
	
	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList <ItemStack> requirements) {
		BptSlotInfo newSlot = slot.clone();
		slot.blockId = delegateTo;
		
		if (BuildCraftAPI.blockBptProps [delegateTo] != null) {
			BuildCraftAPI.blockBptProps [delegateTo].addRequirements(newSlot, context, requirements);
		} else {
			super.addRequirements(newSlot, context, requirements);
		}
	}

	@Override
	public boolean isValid(BptSlotInfo slot,IBptContext context) {
		BptSlotInfo newSlot = slot.clone();
		slot.blockId = delegateTo;
		
		if (BuildCraftAPI.blockBptProps [delegateTo] != null) {
			return BuildCraftAPI.blockBptProps[delegateTo].isValid(
					newSlot, context);
		} else {
			return super.isValid(newSlot, context);
		}
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		BptSlotInfo newSlot = slot.clone();
		slot.blockId = delegateTo;
		
		if (BuildCraftAPI.blockBptProps [delegateTo] != null) {
			BuildCraftAPI.blockBptProps [delegateTo].rotateLeft(newSlot, context);
		} else {
			super.rotateLeft(newSlot, context);
		}
	}
	
}
