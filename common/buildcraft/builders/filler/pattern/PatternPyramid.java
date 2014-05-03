/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import net.minecraft.world.World;

import buildcraft.api.blueprints.SchematicMask;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternPyramid extends FillerPattern {

	// TODO: These parameters need to be settable from the filler
	private boolean param1 = true;

	public PatternPyramid() {
		super("pyramid");
	}

	@Override
	public Template getTemplate (Box box, World world) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		Template bpt = new Template(xMax - xMin + 1, yMax - yMin + 1, zMax - zMin + 1);

		int xSize = xMax - xMin + 1;
		int zSize = zMax - zMin + 1;

		int step = 0;
		int height;

		int stepY = 1;

		if (param1) {
			stepY = 1;
		} else {
			stepY = -1;
		}

		if (stepY == 1) {
			height = yMin;
		} else {
			height = yMax;
		}

		while (step <= xSize / 2 && step <= zSize / 2 && height >= yMin && height <= yMax) {
			for (int x = xMin + step; x <= xMax - step; ++x) {
				for (int z = zMin + step; z <= zMax - step; ++z) {
					bpt.contents [x - xMin][height - yMin][z - zMin] = new SchematicMask(true);
				}
			}

			step++;
			height += stepY;
		}

		return bpt;
	}
}
