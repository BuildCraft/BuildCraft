package net.minecraft.src.buildcraft.api.bptblocks;

import net.minecraft.src.IInventory;
import net.minecraft.src.buildcraft.api.BptBlock;
import net.minecraft.src.buildcraft.api.BptSlotInfo;
import net.minecraft.src.buildcraft.api.IBptContext;

public class BptBlockInventory extends BptBlock {

	public BptBlockInventory(int blockId) {
		super(blockId);
		
	}
	
	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		super.buildBlock(slot, context);
		
		IInventory inv = (IInventory) context.world().getBlockTileEntity(slot.x, slot.y, slot.z);
		
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			inv.setInventorySlotContents(i, null);
		}
		
	}

}
