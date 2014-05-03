/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network;

/**
 * Keeps track of the indices to use when writing data to payload arrays. Internal use only.
 */
public class IndexInPayload {
	public int intIndex = 0;
	public int floatIndex = 0;
	public int stringIndex = 0;

	public IndexInPayload(int intIndex, int floatIndex, int stringIndex) {
		this.intIndex = intIndex;
		this.floatIndex = floatIndex;
		this.stringIndex = stringIndex;
	}
}
