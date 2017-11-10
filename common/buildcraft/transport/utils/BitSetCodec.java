/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.utils;

import java.util.BitSet;

public class BitSetCodec {
	public byte encode(BitSet set) {
		byte result = 0;
		for (byte i = 0; i < 8; i++) {
			result |= set.get(i) ? (1 << i) : 0;
		}
		return result;
	}

	public void decode(byte data, BitSet target) {
		byte localData = data;
		int t = 1;

		target.clear();

		for (byte i = 0; i < 8; i++) {
			target.set(i, (localData & t) != 0);
			t <<= 1;
		}
	}
}
