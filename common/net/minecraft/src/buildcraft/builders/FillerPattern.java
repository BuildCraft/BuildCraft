/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BuildCraftBlockUtil;
import net.minecraft.src.BuildCraftBuilders;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.IBox;
import net.minecraft.src.buildcraft.api.filler.IFillerPattern;

public abstract class FillerPattern implements IFillerPattern {

	protected int id;

	/**
	 * stackToPlace contains the next item that can be place in the world. Null
	 * if there is none. IteratePattern is responsible to decrementing the stack
	 * size if needed. Return true when the iteration process is finished.
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

	public boolean fill(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, ItemStack stackToPlace, World world) {
		boolean found = false;
		int xSlot = 0, ySlot = 0, zSlot = 0;

		for (int y = yMin; y <= yMax && !found; ++y) {
			for (int x = xMin; x <= xMax && !found; ++x) {
				for (int z = zMin; z <= zMax && !found; ++z) {
					if (BuildCraftAPI.softBlock(world.getBlockId(x, y, z))) {
						xSlot = x;
						ySlot = y;
						zSlot = z;

						found = true;
					}
				}
			}
		}

		if (found && stackToPlace != null) {
			stackToPlace.getItem().onItemUse(stackToPlace, BuildCraftAPI.getBuildCraftPlayer(world), world, xSlot, ySlot - 1,
					zSlot, 1);
		}

		return !found;
	}

	public boolean empty(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, World world) {
		boolean found = false;
		int lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE, lastZ = Integer.MAX_VALUE;

		for (int y = yMin; y <= yMax; ++y) {
			found = false;
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (!BuildCraftAPI.softBlock(world.getBlockId(x, y, z))
							&& !BuildCraftAPI.unbreakableBlock(world.getBlockId(x, y, z))) {
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
				BuildCraftBlockUtil.breakBlock(world, lastX, lastY, lastZ);
			}
		}

		return lastX == Integer.MAX_VALUE;
	}

}
