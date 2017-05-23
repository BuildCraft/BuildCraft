/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import gnu.trove.list.array.TByteArrayList;

public class DecompactingBitSet {
    public final int bits;
    private final TByteArrayList bytes = new TByteArrayList();
    private int byteIndex = 0, bitIndex = 0;

    public DecompactingBitSet(int bits, byte[] data) {
        this.bits = bits;
        bytes.addAll(data);
    }

    private int nextBit() {
        int offset = byteIndex;
        byte current = bytes.get(offset);

        int bit = (current >> bitIndex) & 1;

        bitIndex++;
        if (bitIndex == 8) {
            bitIndex = 0;
            byteIndex++;
        }
        return bit;
    }

    public int next() {
        int value = 0;
        for (int i = bits - 1; i >= 0; i--) {
            int bit = nextBit();
            value |= bit << i;
        }
        return value;
    }
}
