/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.API;
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.FillerPattern;

public class FillerFlattener extends FillerPattern {

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.p1().x;
		int yMin = (int) box.p1().y;
		int zMin = (int) box.p1().z;
		
		int xMax = (int) box.p2().x;
		int zMax = (int) box.p2().z;

		int sizeX = xMax - xMin + 1;
		int sizeZ = zMax - zMin + 1;
		
		
		boolean [][] blockedColumns = new boolean [sizeX][sizeZ];
		
		for (int i = 0; i < blockedColumns.length; ++i) {
			for (int j = 0; j < blockedColumns[0].length; ++j) {
				blockedColumns [i][j] = false;
			}
		}

		boolean found = false;
		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;
		
		for (int y = yMin - 1; y >= 0; --y) {
			found = false;
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (!blockedColumns [x - xMin][z - zMin]) {
						if (!API.softBlock(tile.worldObj.getBlockId(x, y, z))) {
							blockedColumns [x - xMin][z - zMin] = true;
						} else {
							found = true;
							lastX = x;
							lastY = y;
							lastZ = z;
						}
					}
				}
			}

			if (!found) {
				break;
			}
		}
		
		if (lastX != Integer.MAX_VALUE && stackToPlace != null) {
			stackToPlace.getItem().onItemUse(stackToPlace, null, tile.worldObj,
				lastX, lastY + 1, lastZ, 0);
		}
		
		if (lastX != Integer.MAX_VALUE) {
			return false;
		}
		
		return empty (xMin, yMin, zMin, xMax, 64 * 2, zMax, tile.worldObj);
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

	@Override
	public int getTextureIndex() {
		return 4 * 16 + 5;
	}

}
