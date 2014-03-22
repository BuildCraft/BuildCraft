/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import buildcraft.api.blueprints.CoordTransformation;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicEntity;

public class BuildingSlotEntity extends BuildingSlot {

	public CoordTransformation transform;
	public SchematicEntity schematic;

	@Override
	public void writeToWorld(IBuilderContext context) {
		schematic.writeToWorld(context, transform);
	}
}
