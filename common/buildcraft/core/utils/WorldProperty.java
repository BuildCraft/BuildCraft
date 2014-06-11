/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public abstract class WorldProperty {

	public ArrayList<DimensionProperty> properties = new ArrayList<DimensionProperty>();

	public boolean get(World world, int x, int y, int z) {
		return getDimension(world).get(x, y, z);
	}

	private DimensionProperty getDimension(World world) {
		int id = world.provider.dimensionId * 2;

		if (world.isRemote) {
			id++;
		}

		while (properties.size() <= id) {
			properties.add(null);
		}

		DimensionProperty result = properties.get(id);

		if (result == null) {
			result = new DimensionProperty(world, this);
			properties.set(id, result);
		}

		return result;
	}

	public void clear() {
		for (DimensionProperty p : properties) {
			p.clear();
		}

		properties.clear();
	}

	protected abstract boolean get(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z);
}
