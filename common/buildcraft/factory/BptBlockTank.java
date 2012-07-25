package buildcraft.factory;

import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;

public class BptBlockTank extends BptBlock {

	public BptBlockTank(int blockId) {
		super(blockId);
	}

	@Override
	public void initializeFromWorld(BptSlotInfo slot, IBptContext context, int x, int y, int z) {

	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		context.world().setBlockAndMetadataWithNotify(slot.x, slot.y, slot.z, slot.blockId, slot.meta);
	}

}
