/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.BitSet;

public class MapChunk {

	private BitSet property;
	private int xPosition, zPosition;

	public MapChunk(int iXPosition, int iZPosition) {
		property = new BitSet(16 * 16);
		xPosition = iXPosition;
		zPosition = iZPosition;
	}

	public boolean get(int xChunk, int zChunk) {
		return property.get(xChunk * 16 + zChunk);
	}

	public void set(int xChunk, int zChunk, boolean value) {
		property.set(xChunk * 16 + zChunk, value);
	}

}
