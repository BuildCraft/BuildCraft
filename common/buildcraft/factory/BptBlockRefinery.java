/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.factory;

import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import net.minecraftforge.common.ForgeDirection;

@Deprecated
public class BptBlockRefinery extends BptBlock {

	public BptBlockRefinery(int blockId) {
		super(blockId);
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		slot.meta = ForgeDirection.values()[slot.meta].getRotation(ForgeDirection.DOWN).ordinal();
	}

	@Override
	public void initializeFromWorld(BptSlotInfo slot, IBptContext context, int x, int y, int z) {
		TileRefinery refinery = (TileRefinery) context.world().getBlockTileEntity(x, y, z);

//		slot.cpt.setInteger("filter0", refinery.getFilter(0));
//		slot.cpt.setInteger("filter1", refinery.getFilter(1));
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		super.buildBlock(slot, context);

		TileRefinery refinery = (TileRefinery) context.world().getBlockTileEntity(slot.x, slot.y, slot.z);

		int filter0 = slot.cpt.getInteger("filter0");
		int filter1 = slot.cpt.getInteger("filter1");
		int filterMeta0 = 0;
		int filterMeta1 = 0;

		if (slot.cpt.hasKey("filterMeta0")) {
			filterMeta0 = slot.cpt.getInteger("filterMeta0");
		}
		if (slot.cpt.hasKey("filterMeta1")) {
			filterMeta1 = slot.cpt.getInteger("filterMeta1");
		}

//		refinery.setFilter(0, filter0, filterMeta0);
//		refinery.setFilter(1, filter1, filterMeta1);
	}

}
