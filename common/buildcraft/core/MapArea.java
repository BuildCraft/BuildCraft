/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;

public class MapArea {

	private LongHashMap chunkMapping = new LongHashMap();

	public boolean get(int x, int z) {
		int xChunk = x >> 4;
		int zChunk = z >> 4;
		long chunkId = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);
		MapChunk property;

		if (!chunkMapping.containsItem(chunkId)) {
			property = new MapChunk(xChunk, zChunk);
			chunkMapping.add(chunkId, property);
		} else {
			property = (MapChunk) chunkMapping.getValueByKey(chunkId);
		}

		return property.get(x & 0xF, z & 0xF);
	}

	public void set(int x, int z, boolean val) {
		int xChunk = x >> 4;
		int zChunk = z >> 4;
		long chunkId = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);
		MapChunk property;

		if (!chunkMapping.containsItem(chunkId)) {
			property = new MapChunk(xChunk, zChunk);
			chunkMapping.add(chunkId, property);
		} else {
			property = (MapChunk) chunkMapping.getValueByKey(chunkId);
		}

		property.set(x & 0xF, z & 0xF, val);
	}

}
