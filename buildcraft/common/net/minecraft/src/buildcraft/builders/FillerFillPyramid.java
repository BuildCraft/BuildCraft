package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.FillerPattern;

public class FillerFillPyramid extends FillerPattern {

	public int stepY;
	
	public FillerFillPyramid (int step) {
		stepY = step;
	}
	
	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.p1().x;
		int yMin = (int) box.p1().y;
		int zMin = (int) box.p1().z;
		
		int xMax = (int) box.p2().x;
		int yMax = (int) box.p2().y;
		int zMax = (int) box.p2().z;
		
		int xSize = xMax - xMin + 1;
		int zSize = zMax - zMin + 1;
		
		int step = 0;
		int height;
		
		if (stepY == 1) {
			height = yMin;
		} else {
			height = yMax;
		}
		
		while (step <= xSize / 2 && step <= zSize / 2 && height >= yMin && height <= yMax) {
			if (!fill(xMin + step, height, zMin + step, xMax - step, height,
					zMax - step, stackToPlace, tile.worldObj)) {
				return false;
			}	
			
			step++;
			height += stepY;
		}
		
		return true;
	}

	
	
	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

	@Override
	public int getTextureIndex() {
		if (stepY == 1) {
			return 4 * 16 + 7;
		} else {
			return 4 * 16 + 8;
		}
	}

}

