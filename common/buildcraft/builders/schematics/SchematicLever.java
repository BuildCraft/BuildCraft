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

public class SchematicLever extends SchematicWallSide {

	@Override
	public void rotateLeft(IBuilderContext context) {
		int status = meta - (meta & 7);

		meta -= status;
		super.rotateLeft(context);
		meta += status;

	}
}
