/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.core.BlockIndex;
import buildcraft.core.Box;

public class BlockScanner implements Iterable<BlockIndex> {

	Box box = new Box();
	World world;

	int x, y, z;
	int iterationsPerCycle;
	int blocksDone = 0;

	class BlockIt implements Iterator<BlockIndex> {

		int it = 0;

		@Override
		public boolean hasNext() {
			return z <= box.zMax && it <= iterationsPerCycle;
		}

		@Override
		public BlockIndex next() {
			BlockIndex index = new BlockIndex(x, y, z);
			it++;
			blocksDone++;

			if (x < box.xMax) {
				x++;
			} else {
				x = box.xMin;

				if (y < box.yMax) {
					y++;
				} else {
					y = box.yMin;

					z++;
				}
			}

			return index;
		}

		@Override
		public void remove() {

		}
	}

	public BlockScanner(Box box, World world, int iterationsPreCycle) {
		this.box = box;
		this.world = world;
		this.iterationsPerCycle = iterationsPreCycle;

		x = box.xMin;
		y = box.yMin;
		z = box.zMin;
	}

	public BlockScanner() {
	}

	@Override
	public Iterator<BlockIndex> iterator() {
		return new BlockIt();
	}

	public int totalBlocks() {
		return box.sizeX() * box.sizeY() * box.sizeZ();
	}

	public int blocksLeft() {
		return totalBlocks() - blocksDone;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setInteger("blocksDone", blocksDone);
		nbt.setInteger("iterationsPerCycle", iterationsPerCycle);
		NBTTagCompound boxNBT = new NBTTagCompound();
		box.writeToNBT(boxNBT);
		nbt.setTag("box", boxNBT);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		x = nbt.getInteger("x");
		y = nbt.getInteger("y");
		z = nbt.getInteger("z");
		blocksDone = nbt.getInteger("blocksDone");
		iterationsPerCycle = nbt.getInteger("iterationsPerCycle");
		box.initialize(nbt.getCompoundTag("box"));
	}

}
