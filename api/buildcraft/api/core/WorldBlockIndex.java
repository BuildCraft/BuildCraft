/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.core;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * This class is a comparable container for block positions. TODO: should this be merged with position?
 */
public class WorldBlockIndex implements Comparable<WorldBlockIndex> {

	public int x;
	public int y;
	public int z;
	public int dimension;

	public WorldBlockIndex() {

	}

	/**
	 * Creates an index for a block located on x, y. z
	 */
	public WorldBlockIndex(World world, int x, int y, int z) {

		dimension = world.provider.getDimensionId();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public WorldBlockIndex(NBTTagCompound c) {
		dimension = c.getInteger("dimension");
		x = c.getInteger("x");
		y = c.getInteger("y");
		z = c.getInteger("z");
	}

	public WorldBlockIndex(Entity entity) {
		dimension = entity.worldObj.provider.getDimensionId();
		x = (int) Math.floor(entity.posX);
		y = (int) Math.floor(entity.posY);
		z = (int) Math.floor(entity.posZ);
	}

	/**
	 * Provides a deterministic and complete ordering of block positions.
	 */
	@Override
	public int compareTo(WorldBlockIndex o) {

		if (o.dimension < dimension) {
			return 1;
		} else if (o.dimension > dimension) {
			return -1;
		} else if (o.x < x) {
			return 1;
		} else if (o.x > x) {
			return -1;
		} else if (o.z < z) {
			return 1;
		} else if (o.z > z) {
			return -1;
		} else if (o.y < y) {
			return 1;
		} else if (o.y > y) {
			return -1;
		} else {
			return 0;
		}
	}

	public void writeTo(NBTTagCompound c) {
		c.setInteger("dimension", dimension);
		c.setInteger("x", x);
		c.setInteger("y", y);
		c.setInteger("z", z);
	}

	@Override
	public String toString() {
		return "{" + dimension + ":" + x + ", " + y + ", " + z + "}";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WorldBlockIndex) {
			WorldBlockIndex b = (WorldBlockIndex) obj;

			return b.dimension == dimension && b.x == x && b.y == y && b.z == z;
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return (dimension * 37 + (x * 37 + y)) * 37 + z;
	}
}
