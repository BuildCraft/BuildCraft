/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders.patterns;

import net.minecraft.world.World;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternStairs extends FillerPattern {

	public PatternStairs() {
		super("stairs");
	}

	@Override
	public int maxParameters() {
		return 2;
	}

	@Override
	public int minParameters() {
		return 2;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return index == 1 ? new PatternParameterXZDir(0) : new PatternParameterYDir(true);
	}

	@Override
	public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
		int xMin = 0;
		int yMin = 0;
		int zMin = 0;

		int xMax = box.sizeX() - 1;
		int yMax = box.sizeY() - 1;
		int zMax = box.sizeZ() - 1;

		int sizeX = xMax - xMin + 1;
		int sizeZ = zMax - zMin + 1;

		Template template = new Template(box.sizeX(), box.sizeY(), box.sizeZ());

		int height, heightStep;

		if (parameters.length >= 1 && parameters[0] != null && !(((PatternParameterYDir) parameters[0]).up)) {
			height = Math.max(yMin, yMax - Math.max(xMax, zMax));
			heightStep = 1;
		} else {
			height = Math.min(yMax, Math.max(xMax, zMax));
			heightStep = -1;
		}

		int param2 = 0;
		if (parameters.length >= 2 && parameters[1] != null) {
			param2 = ((PatternParameterXZDir) parameters[1]).getDirection();
		}

		int[] steps = new int[]{0, 0, 0, 0};

		if (param2 == 0) {
			steps[0] = 1;
		} else if (param2 == 1) {
			steps[1] = 1;
		} else if (param2 == 2) {
			steps[2] = 1;
		} else if (param2 == 3) {
			steps[3] = 1;
		}

		int x1 = xMin, x2 = xMax, z1 = zMin, z2 = zMax;

		if (steps[0] == 1) {
			x1 = xMax - sizeX + 1;
			x2 = x1;
		}

		if (steps[1] == 1) {
			x2 = xMin + sizeX - 1;
			x1 = x2;
		}

		if (steps[2] == 1) {
			z1 = zMax - sizeZ + 1;
			z2 = z1;
		}

		if (steps[3] == 1) {
			z2 = zMin + sizeZ - 1;
			z1 = z2;
		}

		while (x2 - x1 + 1 > 0 && z2 - z1 + 1 > 0 && x2 - x1 < sizeX && z2 - z1 < sizeZ && height >= yMin && height <= yMax) {
			fill(x1, height, z1, x2, height, z2, template);

			x2 += steps[0];
			x1 -= steps[1];
			z2 += steps[2];
			z1 -= steps[3];

			height += heightStep;
		}

		return template;
	}
}
