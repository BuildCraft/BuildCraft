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

public class PatternCylinder extends FillerPattern {

	public PatternCylinder() {
		super("cylinder");
	}

	@Override
	public int maxParameters() {
		return 1;
	}

	@Override
	public int minParameters() {
		return 1;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new PatternParameterHollow(true);
	}

	@Override
	public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
		Template result = new Template(box.sizeX(), box.sizeY(), box.sizeZ());
		boolean filled = parameters.length > 0 && ((PatternParameterHollow) parameters[0]).filled;

		int xMin = 0;
		int yMin = 0;
		int zMin = 0;

		int xMax = box.sizeX() - 1;
		int yMax = box.sizeY() - 1;
		int zMax = box.sizeZ() - 1;

		int xFix = (xMax - xMin) % 2;
		int zFix = (zMax - zMin) % 2;

		int xCenter = (xMax + xMin) / 2
				+ (xMax + xMin < 0 && xFix == 1 ? -1 : 0);
		int zCenter = (zMax + zMin) / 2
				+ (zMax + zMin < 0 && zFix == 1 ? -1 : 0);

		int xRadius = (xMax - xMin) / 2;
		int zRadius = (zMax - zMin) / 2;

		if (xRadius == 0 || zRadius == 0) {
			fill(xMin, yMin, zMin, xMax, yMax, zMax, result);
			return result;
		}

		int dx = xRadius, dz = 0;
		int xChange = zRadius * zRadius * (1 - 2 * xRadius);
		int zChange = xRadius * xRadius;
		int ellipseError = 0;
		int twoASquare = 2 * xRadius * xRadius;
		int twoBSquare = 2 * zRadius * zRadius;
		int stoppingX = twoBSquare * xRadius;
		int stoppingZ = 0;

		if (twoASquare > 0) {
			while (stoppingX >= stoppingZ) {
				if (filled) {
					fillSquare(xCenter, zCenter, dx, dz, xFix, zFix, yMin,
							yMax, result);
				} else {
					fillFourColumns(xCenter, zCenter, dx, dz, xFix, zFix, yMin,
							yMax, result);
				}

				++dz;
				stoppingZ += twoASquare;
				ellipseError += zChange;
				zChange += twoASquare;
				if (2 * ellipseError + xChange > 0) {
					--dx;
					stoppingX -= twoBSquare;
					ellipseError += xChange;
					xChange += twoBSquare;
				}
			}
		}

		dx = 0;
		dz = zRadius;
		xChange = zRadius * zRadius;
		zChange = xRadius * xRadius * (1 - 2 * zRadius);
		ellipseError = 0;
		stoppingX = 0;
		stoppingZ = twoASquare * zRadius;

		if (twoBSquare > 0) {
			while (stoppingX <= stoppingZ) {
				if (filled) {
					fillSquare(xCenter, zCenter, dx, dz, xFix, zFix, yMin,
							yMax, result);
				} else {
					fillFourColumns(xCenter, zCenter, dx, dz, xFix, zFix, yMin,
							yMax, result);
				}

				++dx;
				stoppingX += twoBSquare;
				ellipseError += xChange;
				xChange += twoBSquare;
				if (2 * ellipseError + zChange > 0) {
					--dz;
					stoppingZ -= twoASquare;
					ellipseError += zChange;
					zChange += twoASquare;
				}
			}
		}

		return result;
	}

	private boolean fillSquare(int xCenter, int zCenter, int dx, int dz,
							   int xFix, int zFix, int yMin, int yMax, Template template) {
		int x1, x2, z1, z2;

		x1 = xCenter + dx + xFix;
		z1 = zCenter + dz + zFix;

		x2 = xCenter - dx;
		z2 = zCenter + dz + zFix;

		fill(x2, yMin, z2, x1, yMax, z1, template);

		x1 = xCenter - dx;
		z1 = zCenter - dz;

		fill(x1, yMin, z1, x2, yMax, z2, template);

		x2 = xCenter + dx + xFix;
		z2 = zCenter - dz;

		fill(x1, yMin, z1, x2, yMax, z2, template);

		x1 = xCenter + dx + xFix;
		z1 = zCenter + dz + zFix;

		fill(x2, yMin, z2, x1, yMax, z1, template);

		return true;
	}

	private boolean fillFourColumns(int xCenter, int zCenter, int dx, int dz,
									int xFix, int zFix, int yMin, int yMax, Template template) {
		int x, z;

		x = xCenter + dx + xFix;
		z = zCenter + dz + zFix;
		fill(x, yMin, z, x, yMax, z, template);

		x = xCenter - dx;
		z = zCenter + dz + zFix;
		fill(x, yMin, z, x, yMax, z, template);

		x = xCenter - dx;
		z = zCenter - dz;
		fill(x, yMin, z, x, yMax, z, template);

		x = xCenter + dx + xFix;
		z = zCenter - dz;
		fill(x, yMin, z, x, yMax, z, template);

		return true;
	}

}
