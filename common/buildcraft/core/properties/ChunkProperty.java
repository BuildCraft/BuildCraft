/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.properties;

import java.util.BitSet;

import net.minecraft.world.IBlockAccess;

public class ChunkProperty {

	private BitSet property;
	private int worldHeight;
	private int xPosition, zPosition;
	private IBlockAccess blockAccess;

	public ChunkProperty(IBlockAccess iBlockAccess, int iWorldHeight, int iXPosition, int iZPosition) {
		worldHeight = iWorldHeight;
		property = new BitSet(16 * 16 * worldHeight / 8);
		xPosition = iXPosition;
		zPosition = iZPosition;
		blockAccess = iBlockAccess;
	}

	public boolean get(int xChunk, int y, int zChunk) {
		return property.get(xChunk * worldHeight * 16 + y * 16 + zChunk);
	}

	public void set(int xChunk, int y, int zChunk, boolean value) {
		property.set(xChunk * worldHeight * 16 + y * 16 + zChunk, value);
	}
}
