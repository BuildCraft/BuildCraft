/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.HashMap;

import net.minecraft.world.ChunkCoordIntPair;

import buildcraft.api.core.NetworkData;

public class MapArea {

	// TODO: This can exceed 32k of data. Generalize the slicing code used
	// in tiles.
	@NetworkData
	private HashMap chunkMapping = new HashMap<Long, MapChunk>();

	public boolean get(int x, int z) {
		int xChunk = x >> 4;
		int zChunk = z >> 4;
		long chunkId = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);
		MapChunk property;

		if (!chunkMapping.containsKey(chunkId)) {
			return false;
		} else {
			property = (MapChunk) chunkMapping.get(chunkId);
			return property.get(x & 0xF, z & 0xF);
		}
	}

	public void set(int x, int z, boolean val) {
		int xChunk = x >> 4;
		int zChunk = z >> 4;
		long chunkId = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);
		MapChunk property;

		if (!chunkMapping.containsKey(chunkId)) {
			property = new MapChunk();
			chunkMapping.put(chunkId, property);
		} else {
			property = (MapChunk) chunkMapping.get(chunkId);
		}

		property.set(x & 0xF, z & 0xF, val);
	}

}
