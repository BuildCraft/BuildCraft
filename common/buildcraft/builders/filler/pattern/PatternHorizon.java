/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternHorizon extends FillerPattern {

	public PatternHorizon() {
		super("horizon");
	}

	@Override
	public Template getBlueprint(Box box) {
		/*
		int xMin = 0;
		int yMin = 0;
		int zMin = 0;

		int xMax = box.sizeX() - 1;
		int zMax = box.sizeZ() - 1;

		Template template = new Template ();

		flatten(xMin, 1, zMin, xMax, yMin - 1, zMax, template);
		empty(xMin, yMin, zMin, xMax, 128, template);*/

		// FIXME: This one still needs to be fixed, taking into account
		// world specific data somehow

		return new Template (0, 0, 0);
	}
}
