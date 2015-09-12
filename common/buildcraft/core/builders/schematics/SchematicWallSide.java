/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicWallSide extends SchematicBlock {
	@Override
	public boolean canPlaceInWorld(IBuilderContext context, int x, int y, int z) {
		final int yPos = 0;
		final int yNeg = 5;
		final int xPos = 2;
		final int xNeg = 1;
		final int zPos = 4;
		final int zNeg = 3;

		switch (meta & 7) {
			case xPos:
				return !context.world().isAirBlock(x + 1, y, z);
			case xNeg:
				return !context.world().isAirBlock(x - 1, y, z);
			case yPos:
				return !context.world().isAirBlock(x, y + 1, z);
			case yNeg:
				return !context.world().isAirBlock(x, y - 1, z);
			case zPos:
				return !context.world().isAirBlock(x, y, z + 1);
			case zNeg:
				return !context.world().isAirBlock(x, y, z - 1);
		}
		return true;
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		final int xPos = 2;
		final int xNeg = 1;
		final int zPos = 4;
		final int zNeg = 3;

		switch (meta) {
		case xPos:
			meta = zPos;
			break;
		case zNeg:
			meta = xPos;
			break;
		case xNeg:
			meta = zNeg;
			break;
		case zPos:
			meta = xNeg;
			break;
		}
	}
}
