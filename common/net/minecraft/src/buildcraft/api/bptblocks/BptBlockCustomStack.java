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
import net.minecraft.src.buildcraft.api.IBptContext;

public class BptBlockCustomStack extends BptBlock {

	final ItemStack customStack;
	
	public BptBlockCustomStack (int blockId, ItemStack customStack) {
		super (blockId);
		
		this.customStack = customStack;
	}
	

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList <ItemStack> requirements) {
		requirements.add (customStack.copy());
	}
	
}
