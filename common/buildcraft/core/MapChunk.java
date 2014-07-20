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

import buildcraft.api.core.NetworkData;

public class MapChunk {

	@NetworkData
	private BitSet property;

	@NetworkData
	private boolean fullSet = false;

	public MapChunk() {
	}

	public boolean get(int xChunk, int zChunk) {
		if (fullSet) {
			return true;
		} else if (property == null) {
			return false;
		} else {
			return property.get(xChunk * 16 + zChunk);
		}
	}

	public void set(int xChunk, int zChunk, boolean value) {
		if (property == null && !fullSet) {
			property = new BitSet(16 * 16);
		}

		if (property == null && !value) {
			property = new BitSet(16 * 16);
			property.flip(0, 16 * 16 - 1);
		}

		if (property != null) {
			property.set(xChunk * 16 + zChunk, value);
		}

		if (value && !fullSet) {
			if (property.nextClearBit(0) >= 16 * 16) {
				property = null;
				fullSet = true;
			}
		}
	}

}
