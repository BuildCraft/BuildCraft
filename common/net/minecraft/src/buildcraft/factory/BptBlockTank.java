package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.buildcraft.api.BptBlock;
import net.minecraft.src.buildcraft.api.BptSlotInfo;
import net.minecraft.src.buildcraft.api.IBptContext;

public class BptBlockTank extends BptBlock {

	public BptBlockTank(int blockId) {
		super(blockId);
	}
	
	@Override
	public void initializeFromWorld(BptSlotInfo slot, IBptContext context, int x, int y, int z) {
		
	}
	
	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		context.world().setBlockAndMetadataWithNotify(slot.x, slot.y, slot.z,
				slot.blockId, slot.meta);
	}

}
