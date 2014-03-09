/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicTank extends SchematicTile {

	@Override
	public void readFromWorld(IBuilderContext context, int x, int y, int z) {

	}

	@Override
	public void writeToWorld(IBuilderContext context, int x, int y, int z) {
		context.world().setBlock(x, y, z, block, meta, 3);
	}

}
