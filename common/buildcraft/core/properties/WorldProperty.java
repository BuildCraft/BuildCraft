/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.properties;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.core.IWorldProperty;

public abstract class WorldProperty implements IWorldProperty {

	public HashMap<Integer, DimensionProperty> properties = new HashMap<Integer, DimensionProperty>();

	@Override
	public synchronized boolean get(World world, int x, int y, int z) {
		return getDimension(world).get(x, y, z);
	}

	private DimensionProperty getDimension(World world) {
		int id = world.provider.dimensionId * 2;

		if (world.isRemote) {
			id++;
		}

		DimensionProperty result = properties.get(id);

		if (result == null) {
			result = new DimensionProperty(world, this);
			properties.put(id, result);
		}

		return result;
	}

	@Override
	public void clear() {
		for (DimensionProperty p : properties.values()) {
			if (p != null) {
				p.clear();
			}
		}

		properties.clear();
	}

	protected abstract boolean get(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z);
}
