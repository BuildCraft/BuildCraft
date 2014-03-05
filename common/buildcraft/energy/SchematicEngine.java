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
import buildcraft.api.blueprints.Schematic;
import buildcraft.api.blueprints.IBuilderContext;

public class SchematicEngine extends Schematic {

	@Override
	public void rotateLeft(IBuilderContext context) {
		int o = cpt.getInteger("orientation");

		o = ForgeDirection.values()[o].getRotation(ForgeDirection.DOWN).ordinal();

		cpt.setInteger("orientation", o);
	}

	@Override
	public void readFromWorld(IBuilderContext context, int x, int y, int z) {
		TileEngine engine = (TileEngine) context.world().getTileEntity(x, y, z);

		cpt.setInteger("orientation", engine.orientation.ordinal());
	}

	@Override
	public void writeToWorld(IBuilderContext context) {
		context.world().setBlock(x, y, z, block, meta,1);

		TileEngine engine = (TileEngine) context.world().getTileEntity(x, y, z);

		engine.orientation = ForgeDirection.getOrientation(cpt.getInteger("orientation"));
	}

}
