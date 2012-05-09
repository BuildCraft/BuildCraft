/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import java.util.LinkedList;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.BptBlock;
import net.minecraft.src.buildcraft.api.BptSlotInfo;
import net.minecraft.src.buildcraft.api.IBptContext;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockUtils;

public class BptBlockAutoWorkbench extends BptBlock {
	
	public BptBlockAutoWorkbench(int blockId) {
		super(blockId);
	}

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList<ItemStack> requirements) {
		super.addRequirements(slot, context, requirements);
		
		BptBlockUtils.requestInventoryContents(slot, context, requirements);
	}
	
	@Override
	public void initializeFromWorld(BptSlotInfo bptSlot, IBptContext context, int x,
			int y, int z) {
		IInventory inventory = (IInventory) context.world()
				.getBlockTileEntity(x, y, z);

		BptBlockUtils.initializeInventoryContents(bptSlot, context, inventory);
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		super.buildBlock(slot, context);
		
		IInventory inventory = (IInventory) context.world()
				.getBlockTileEntity(slot.x, slot.y, slot.z);

		BptBlockUtils.buildInventoryContents(slot, context, inventory);
	}
	
}
