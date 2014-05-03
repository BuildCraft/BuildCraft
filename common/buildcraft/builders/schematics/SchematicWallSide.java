/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicWallSide extends SchematicBlock {

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
