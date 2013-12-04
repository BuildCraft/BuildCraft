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

public class PatternHorizon extends FillerPattern {

	public PatternHorizon() {
		super("horizon");
	}

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int zMax = (int) box.pMax().z;

		if (stackToPlace != null && flatten(xMin, 1, zMin, xMax, yMin - 1, zMax, tile.worldObj, stackToPlace)) {
			return false;
		}
		return !empty(xMin, yMin, zMin, xMax, tile.worldObj.getActualHeight(), zMax, tile.worldObj);
	}
}
