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
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternClear extends FillerPattern {

	public PatternClear() {
		super("clear");
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

		return bpt;
	}
}
