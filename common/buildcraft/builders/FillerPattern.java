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
import net.minecraft.world.World;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.IBox;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BlockUtil;

public abstract class FillerPattern implements IFillerPattern {

	protected int id;

	/**
	 * stackToPlace contains the next item that can be place in the world. Null if there is none. IteratePattern is responsible to decrementing the stack size
	 * if needed. Return true when the iteration process is finished.
	 */
	@Override
	public abstract boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace);

	@Override
	public abstract String getTextureFile();

	@Override
	public abstract int getTextureIndex();

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return this.id;
	}

	/**
	 * Attempt to fill blocks in the area.
	 *
	 * Return false if the process failed.
	 *
	 */
	public boolean fill(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, ItemStack stackToPlace, World world) {
		boolean found = false;
		int xSlot = 0, ySlot = 0, zSlot = 0;

		for (int y = yMin; y <= yMax && !found; ++y) {
			for (int x = xMin; x <= xMax && !found; ++x) {
				for (int z = zMin; z <= zMax && !found; ++z) {
					if (!BlockUtil.canChangeBlock(world, x, y, z))
						return false;
					if (BlockUtil.isSoftBlock(world, x, y, z)) {
						xSlot = x;
						ySlot = y;
						zSlot = z;

						found = true;
					}
				}
			}
		}

		if (found && stackToPlace != null) {
			stackToPlace.getItem().onItemUse(stackToPlace, CoreProxy.proxy.getBuildCraftPlayer(world), world, xSlot, ySlot - 1, zSlot, 1, 0.0f, 0.0f, 0.0f);
		}

		return found;
	}

	/**
	 * Attempt to remove the blocks in the area.
	 *
	 * Return false if is the process failed.
	 *
	 */
	public boolean empty(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, World world) {
		boolean found = false;
		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

		for (int y = yMax; y >= yMin; y--) {
			found = false;
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (!BlockUtil.canChangeBlock(world, x, y, z))
						return false;
					if (!BlockUtil.isSoftBlock(world, x, y, z)) {
						found = true;
						lastX = x;
						lastY = y;
						lastZ = z;
					}
				}
			}

			if (found) {
				break;
			}
		}

		if (lastX != Integer.MAX_VALUE) {
			if (BuildCraftBuilders.fillerDestroy) {
				world.setBlockWithNotify(lastX, lastY, lastZ, 0);
			} else {
				BlockUtil.breakBlock(world, lastX, lastY, lastZ, 20);
			}
			return true;
		}

		return false;
	}

}
