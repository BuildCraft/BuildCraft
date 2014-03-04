/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.bptblocks;

import buildcraft.api.blueprints.IBptContext;

public class BptBlockPiston extends BptBlockRotateMeta {

	public BptBlockPiston() {
		super(new int[] { 2, 5, 3, 4 }, true);
	}

	@Override
	public void buildBlock(IBptContext context) {
		int localMeta = meta & 7;

		context.world().setBlock(x, y, z, block, localMeta, 3);
	}

}
