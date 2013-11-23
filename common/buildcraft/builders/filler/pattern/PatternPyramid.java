/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.filler.pattern;

import buildcraft.api.core.IBox;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class PatternPyramid extends FillerPattern {

	public PatternPyramid() {
		super("pyramid");
	}

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		int xSize = xMax - xMin + 1;
		int zSize = zMax - zMin + 1;

		int step = 0;
		int height;

		int stepY;

		if (tile.yCoord <= yMin) {
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
			if (fill(xMin + step, height, zMin + step, xMax - step, height, zMax - step, stackToPlace, tile.worldObj))
				return false;

			step++;
			height += stepY;
		}

		return true;
	}
}
