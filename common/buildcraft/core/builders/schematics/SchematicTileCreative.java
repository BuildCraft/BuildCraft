/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders.schematics;

import buildcraft.api.blueprints.BuildingPermission;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicTileCreative extends SchematicTile {

	@Override
	public BuildingPermission getBuildingPermission() {
		return BuildingPermission.CREATIVE_ONLY;
	}

}
