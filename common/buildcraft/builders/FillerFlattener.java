/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import buildcraft.api.core.IBox;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;

public class FillerFlattener extends FillerPattern {


	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		if (flatten(xMin, 1, zMin, xMax, yMin - 1, zMax, tile.worldObj, stackToPlace)) {
			return false;
		}
		return !empty(xMin, yMin, zMin, xMax, yMax, zMax, tile.worldObj);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Icon getTexture() {
		return BuilderProxyClient.fillerFlattenTexture;
	}

	@Override
	public String getName() {
		return "Flatten";
	}
}
