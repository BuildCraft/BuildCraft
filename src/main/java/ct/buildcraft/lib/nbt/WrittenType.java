/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import ct.buildcraft.api.core.InvalidInputDataException;

public enum WrittenType {
    BYTE(1, (1 << 8) - 1),
    SHORT(2, (1 << 16) - 1),
    MEDIUM(3, (1 << 24) - 1),
    INT(4, Integer.MAX_VALUE);

    public static final WrittenType[] ORDERED_VALUES = { BYTE, SHORT, MEDIUM, INT };

    public final int numBytes;
    private final int maxStorableValue;

    WrittenType(int numBytes, int maxStorableValue) {
        this.maxStorableValue = maxStorableValue;
        this.numBytes = numBytes;
    }

    public static WrittenType getForSize(int size) {
        for (WrittenType type : ORDERED_VALUES) {
            if (size < type.maxStorableValue) {
                return type;
            }
        }
        throw new IllegalArgumentException("Waaaaay too big index list (" + size + ")");
    }

    public void writeType(DataOutput to) throws IOException {
        to.writeByte(numBytes - 1);
    }

    public static WrittenType readType(DataInput in) throws IOException {
        byte val = in.readByte();
        for (WrittenType type : ORDERED_VALUES) {
            if (val == type.numBytes - 1) {
                return type;
            }
        }
        throw new InvalidInputDataException("Incorrect size given, expected any of [0, 1, 2, 3] but got " + val);
    }

    public void writeIndex(DataOutput out, int index) throws IOException {
        if (index > maxStorableValue) {
            throw new IllegalArgumentException("Tried to write a value that was too large! (" + index + " > "
                + maxStorableValue + " for " + this + ")");
        }
        switch (this) {
            case BYTE: {
                out.writeByte(index);
                break;
            }
            case SHORT: {
                out.writeShort(index);
                break;
            }
            case MEDIUM: {
                out.writeByte(index & 0xff);
                out.writeShort(index >> 8);
                break;
            }
            default:
            case INT: {
                out.writeInt(index);
                break;
            }
        }
    }

    public int readIndex(DataInput in) throws IOException {
        switch (this) {
            case BYTE:
                return in.readUnsignedByte();
            case SHORT:
                return in.readUnsignedShort();
            case MEDIUM:
                int val = in.readUnsignedByte();
                val |= in.readUnsignedShort() << 8;
                return val;
            default:
            case INT:
                return in.readInt();
        }
    }
}
