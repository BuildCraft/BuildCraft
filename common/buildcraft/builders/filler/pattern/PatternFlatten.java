/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import buildcraft.api.blueprints.SchematicMask;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternFlatten extends FillerPattern {

	public PatternFlatten() {
		super("flatten");
	}

	@Override
	public Template getBlueprint (Box box) {
		int xMin = (int) box.pMin().x;
		int yMin = 1;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		Template bpt = new Template(xMax - xMin + 1, yMax - yMin + 1, zMax - zMin + 1);

		boolean found = false;
		for (int y = yMax; y >= yMin; --y) {
			for (int x = xMin; x <= xMax && !found; ++x) {
				for (int z = zMin; z <= zMax && !found; ++z) {
					bpt.contents[x - xMin][y - yMin][z - zMin] = new SchematicMask(true);
				}
			}
		}

		return bpt;
	}
}
