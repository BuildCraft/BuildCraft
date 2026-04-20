/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc.data;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;

public class CompactingBitSet {
    public final int bits;
    private final ByteArrayList bytes = new ByteArrayList();
    private int bitIndex = 0;

    public CompactingBitSet(int bits) {
        this.bits = bits;
    }

    public void ensureCapacityValues(int values) {
        ensureCapacityBits(values * bits);
    }

    public void ensureCapacityBits(int totalBits) {
        bytes.ensureCapacity(totalBits / 8 + 1);
    }

    private void appendBit(int bit) {
        if (bitIndex == 0) {
            bytes.add((byte) 0);
        }
        int offset = bytes.size() - 1;
        byte current = bytes.getByte(offset);
        if ((bit & 1) == 1) {
            current |= 1 << bitIndex;
            bytes.set(offset, current);
        }
        bitIndex++;
        if (bitIndex == 8) {
            bitIndex = 0;
        }
    }

    public void append(int value) {
        for (int i = bits - 1; i >= 0; i--) {
            int bit = (value >> i) & 1;
            appendBit(bit);
        }
    }

    public byte[] getBytes() {
        return bytes.elements();
    }
}
