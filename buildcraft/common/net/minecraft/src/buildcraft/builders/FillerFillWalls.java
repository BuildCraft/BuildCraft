package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.FillerPattern;

public class FillerFillWalls extends FillerPattern {

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.p1().x;
		int yMin = (int) box.p1().y;
		int zMin = (int) box.p1().z;
		
		int xMax = (int) box.p2().x;
		int yMax = (int) box.p2().y;
		int zMax = (int) box.p2().z;
		
		
		if (!fill (xMin, yMin, zMin, xMax, yMin, zMax, stackToPlace, tile.worldObj)) {
			return false;
		}
		
		if (!fill (xMin, yMin, zMin, xMin, yMax, zMax, stackToPlace, tile.worldObj)) {
			return false;
		}
		
		if (!fill (xMin, yMin, zMin, xMax, yMax, zMin, stackToPlace, tile.worldObj)) {
			return false;
		}
		
		if (!fill (xMax, yMin, zMin, xMax, yMax, zMax, stackToPlace, tile.worldObj)) {
			return false;
		}

		if (!fill (xMin, yMin, zMax, xMax, yMax, zMax, stackToPlace, tile.worldObj)) {
			return false;
		}
				
		if (!fill (xMin, yMax, zMin, xMax, yMax, zMax, stackToPlace, tile.worldObj)) {
			return false;
		}
		
		return true;
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

	@Override
	public int getTextureIndex() {
		return 4 * 16 + 6;
	}

}
