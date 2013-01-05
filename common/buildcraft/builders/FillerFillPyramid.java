/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.core.IBox;
import buildcraft.api.power.IPowerProvider;
import buildcraft.core.DefaultProps;

public class FillerFillPyramid extends FillerPattern {

	@Override
	public float iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace, IPowerProvider power) {
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
		
		float powerUsed = -1;

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
			powerUsed = fill(xMin + step, height, zMin + step, xMax - step, height, zMax - step, stackToPlace, tile.worldObj, power);
			if (powerUsed>=0)
				return powerUsed;

			step++;
			height += stepY;
		}

		return powerUsed;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex() {
		return 4 * 16 + 7;
	}

	@Override
	public String getName() {
		return "Pyramid";
	}

}
