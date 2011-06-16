package net.minecraft.src.buildcraft.api;

import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.core.Utils;

public abstract class FillerPattern {
	
	/**
	 * [blablabla]
	 * 
	 * stackToPlace contains the next item that can be place in the world. Null
	 * if there is none. IteratePattern is responsible to decrementing the 
	 * stack size if needed.
	 * Return true when the iteration process is finished.
	 */
	public abstract boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace);

	public abstract String getTextureFile ();
	
	public abstract int getTextureIndex ();
	
	public boolean fill(int xMin, int yMin, int zMin, int xMax, int yMax,
			int zMax, ItemStack stackToPlace, World world) {
		boolean found = false;
		int xSlot = 0, ySlot = 0, zSlot = 0;

		for (int y = yMin; y <= yMax && !found; ++y) {
			for (int x = xMin; x <= xMax && !found; ++x) {
				for (int z = zMin; z <= zMax && !found; ++z) {
					if (world.getBlockId(x, y, z) == 0
							|| world.getBlockId(x, y, z) == Block.waterMoving.blockID
							|| world.getBlockId(x, y, z) == Block.waterStill.blockID) {
						xSlot = x;
						ySlot = y;
						zSlot = z;

						found = true;
					}
				}
			}
		}

		if (found && stackToPlace != null) {
			stackToPlace.getItem().onItemUse(stackToPlace, null, world,
					xSlot, ySlot + 1, zSlot, 0);
		}
		
		return !found;
	}
	
	public boolean empty(int xMin, int yMin, int zMin, int xMax, int yMax,
			int zMax, World world) {
		boolean found = false;
		int lastX = -1, lastY = -1, lastZ = -1;
		
		for (int y = yMin; y <= yMax; ++y) {
			found = false;
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (world.getBlockId(x, y, z) != 0
							&& world.getBlockId(x, y, z) != Block.waterMoving.blockID
							&& world.getBlockId(x, y, z) != Block.waterStill.blockID) {
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
		
		if (lastX != -1) {
			Utils.breakBlock(world, lastX, lastY, lastZ);
		}
				
		
		return lastX == -1;
	}

}
