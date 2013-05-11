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
		int zMax = (int) box.pMax().z;

		int sizeX = xMax - xMin + 1;
		int sizeZ = zMax - zMin + 1;

		boolean[][] blockedColumns = new boolean[sizeX][sizeZ];

		boolean found;
		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

		for (int y = yMin - 1; y >= 0; --y) {
			found = false;
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (!BlockUtil.canChangeBlock(tile.worldObj, x, y, z)) {
						return true;
					}
					if (!blockedColumns[x - xMin][z - zMin]) {
						if (!BlockUtil.isSoftBlock(tile.worldObj, x, y, z)) {
							blockedColumns[x - xMin][z - zMin] = true;
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
			BlockUtil.breakBlock(tile.worldObj, lastX, lastY - 1, lastZ);
			stackToPlace.getItem().onItemUse(stackToPlace, CoreProxy.proxy.getBuildCraftPlayer(tile.worldObj), tile.worldObj, lastX, lastY - 1, lastZ, 1, 0.0f,
					0.0f, 0.0f);
		}

		if (lastX != Integer.MAX_VALUE) {
			return false;
		}

		return !empty(xMin, yMin, zMin, xMax, 64 * 4, zMax, tile.worldObj);
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
