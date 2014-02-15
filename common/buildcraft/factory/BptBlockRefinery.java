/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import net.minecraftforge.common.util.ForgeDirection;

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
		TileRefinery refinery = (TileRefinery) context.world().getTileEntity(x, y, z);

//		slot.cpt.setInteger("filter0", refinery.getFilter(0));
//		slot.cpt.setInteger("filter1", refinery.getFilter(1));
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		super.buildBlock(slot, context);

		TileRefinery refinery = (TileRefinery) context.world().getTileEntity(slot.x, slot.y, slot.z);

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
