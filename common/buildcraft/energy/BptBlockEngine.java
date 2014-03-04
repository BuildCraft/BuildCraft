/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.IBptContext;

public class BptBlockEngine extends BptBlock {

	@Override
	public void rotateLeft(IBptContext context) {
		int o = cpt.getInteger("orientation");

		o = ForgeDirection.values()[o].getRotation(ForgeDirection.DOWN).ordinal();

		cpt.setInteger("orientation", o);
	}

	@Override
	public void initializeFromWorld(IBptContext context, int x, int y, int z) {
		TileEngine engine = (TileEngine) context.world().getTileEntity(x, y, z);

		cpt.setInteger("orientation", engine.orientation.ordinal());
	}

	@Override
	public void buildBlock(IBptContext context) {
		context.world().setBlock(x, y, z, block, meta,1);

		TileEngine engine = (TileEngine) context.world().getTileEntity(x, y, z);

		engine.orientation = ForgeDirection.getOrientation(cpt.getInteger("orientation"));
	}

}
