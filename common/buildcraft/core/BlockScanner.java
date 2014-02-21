/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.Iterator;

import net.minecraft.world.World;

public class BlockScanner implements Iterable<BlockWrapper> {

	Box box;
	World world;

	int x, y, z;
	int iterations;

	class BlockIt implements Iterator<BlockWrapper> {

		int it = 0;

		@Override
		public boolean hasNext() {
			return it <= iterations;
		}

		@Override
		public BlockWrapper next() {
			if (x <= box.xMax) {
				x++;
			} else {
				x = box.xMin;

				if (y <= box.yMax) {
					y++;
				} else {
					y = box.yMin;

					if (z <= box.zMax) {
						z++;
					} else {
						z = box.zMin;
					}
				}
			}

			it++;

			BlockWrapper w = new BlockWrapper();
			w.index = new BlockIndex(x, y, z);
			w.block = world.getBlock (x, y, z);
			w.tile = world.getTileEntity(x, y, z);

			return w;
		}

		@Override
		public void remove() {

		}
	}

	public  BlockScanner (Box box, World world, int iterations) {
		this.box = box;
		this.world = world;
		this.iterations = iterations;

		x = box.xMin;
		y = box.yMin;
		z = box.zMin;
	}

	@Override
	public Iterator<BlockWrapper> iterator() {
		return new BlockIt();
	}

}
