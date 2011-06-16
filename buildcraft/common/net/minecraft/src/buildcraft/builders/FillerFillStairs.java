package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.FillerPattern;

public class FillerFillStairs extends FillerPattern {

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.p1().x;
		int yMin = (int) box.p1().y;
		int zMin = (int) box.p1().z;
		
		int xMax = (int) box.p2().x;
		int yMax = (int) box.p2().y;
		int zMax = (int) box.p2().z;
		
		int step = 0;
		int height = yMin;

		int steps [] = new int [] {0,0,0,0};
		
		if (tile.xCoord == xMin - 1) {
			steps [0] = 1;
		} else if (tile.xCoord == xMax + 1) {
			steps [1] = 1;
		} else if (tile.zCoord == zMin - 1) {
			steps [2] = 1;
		} else if (tile.zCoord == zMax + 1) {
			steps [3] = 1;
		}
		
		while (xMax - xMin + 1 > 0 && zMax - zMin + 1 > 0 && height >= yMin
				&& height <= yMax) {
			if (!fill(xMin + step, height, zMin + step, xMax - step, height,
					zMax - step, stackToPlace, tile.worldObj)) {
				return false;
			}	
			
			xMin += steps [0];
			xMax -= steps [1];
			zMin += steps [2];
			zMax -= steps [3];

			height ++;
		}
		
		return true;
	}	
	
	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

	@Override
	public int getTextureIndex() {
		return 4 * 16 + 9;
	}

}

