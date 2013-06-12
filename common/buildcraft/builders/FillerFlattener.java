/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import buildcraft.api.core.IBox;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FillerFlattener extends FillerPattern {

	@Override
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace) {
		int xMin = (int) box.pMin().x;
		int yMin = (int) box.pMin().y;
		int zMin = (int) box.pMin().z;

		int xMax = (int) box.pMax().x;
		int yMax = (int) box.pMax().y;
		int zMax = (int) box.pMax().z;

		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

		boolean found = false;
		for (int x = xMin; x <= xMax && !found; ++x) {
			for (int z = zMin; z <= zMax && !found; ++z) {
				for (int y = yMin - 1; y > 0; --y) {
					if (!BlockUtil.canChangeBlock(tile.worldObj, x, y, z) || !BlockUtil.isSoftBlock(tile.worldObj, x, y, z)) {
						break;
					} else {
						found = true;
						lastX = x;
						lastY = y;
						lastZ = z;
					}
				}
			}
		}

		if (lastX != Integer.MAX_VALUE) {
			if (stackToPlace != null) {
				BlockUtil.breakBlock(tile.worldObj, lastX, lastY, lastZ);
				stackToPlace.getItem().onItemUse(stackToPlace, CoreProxy.proxy.getBuildCraftPlayer(tile.worldObj), tile.worldObj, lastX, lastY - 1, lastZ, 1, 0.0f, 0.0f, 0.0f);
			}
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
