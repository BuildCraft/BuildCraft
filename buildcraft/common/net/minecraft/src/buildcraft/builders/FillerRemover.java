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
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.FillerPattern;

public class FillerRemover extends FillerPattern {

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.p1().x;
		int yMin = (int) box.p1().y;
		int zMin = (int) box.p1().z;
		
		int xMax = (int) box.p2().x;
		int yMax = (int) box.p2().y;
		int zMax = (int) box.p2().z;
						
		return empty (xMin, yMin, zMin, xMax, yMax, zMax, tile.worldObj);
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

	@Override
	public int getTextureIndex() {
		return 4 * 16 + 4;
	}

}
