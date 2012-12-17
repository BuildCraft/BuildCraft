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
import buildcraft.core.DefaultProps;

public class FillerFillWalls extends FillerPattern {

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		if (fill(xMin, yMin, zMin, xMax, yMin, zMax, stackToPlace, tile.worldObj))
			return false;

		if (fill(xMin, yMin, zMin, xMin, yMax, zMax, stackToPlace, tile.worldObj))
			return false;

		if (fill(xMin, yMin, zMin, xMax, yMax, zMin, stackToPlace, tile.worldObj))
			return false;

		if (fill(xMax, yMin, zMin, xMax, yMax, zMax, stackToPlace, tile.worldObj))
			return false;

		if (fill(xMin, yMin, zMax, xMax, yMax, zMax, stackToPlace, tile.worldObj))
			return false;

		if (fill(xMin, yMax, zMin, xMax, yMax, zMax, stackToPlace, tile.worldObj))
			return false;

		return true;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex() {
		return 4 * 16 + 6;
	}

	@Override
	public String getName() {
		return "Walls";
	}

}
