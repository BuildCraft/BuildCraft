/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.data.NbtSquishConstants;

import buildcraft.lib.misc.data.DecompactingBitSet;

class NbtSquishMapReader {
    private final NbtSquishMap map = new NbtSquishMap();

    public static NbtSquishMap read(DataInput in) throws IOException {
        return new NbtSquishMapReader().readInternal(in);
    }

    private NbtSquishMap readInternal(DataInput in) throws IOException {
        WrittenType type = WrittenType.readType(in);
        int flags = in.readInt();

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_BYTES)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                map.bytes.add(in.readByte());
            }
        }

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_SHORTS)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                map.shorts.add(in.readShort());
            }
        }

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_INTS)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                map.ints.add(in.readInt());
            }
        }

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_LONGS)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                map.longs.add(in.readLong());
            }
        }

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_FLOATS)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                map.floats.add(in.readFloat());
            }
        }

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_DOUBLES)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                map.doubles.add(in.readDouble());
            }
        }

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_BYTE_ARRAYS)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                int arraySize = in.readUnsignedShort();
                TByteArrayList list = new TByteArrayList();
                for (int j = 0; j < arraySize; j++) {
                    list.add(in.readByte());
                }
                map.byteArrays.add(list);
            }
        }

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_INT_ARRAYS)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                int arraySize = in.readUnsignedShort();
                TIntArrayList list = new TIntArrayList();
                for (int j = 0; j < arraySize; j++) {
                    list.add(in.readInt());
                }
                map.intArrays.add(list);
            }
        }

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_STRINGS)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                int length = in.readUnsignedShort();
                byte[] bytes = new byte[length];
                in.readFully(bytes);
                map.strings.add(new String(bytes, StandardCharsets.UTF_8));
            }
        }

        if (isFlag(flags, NbtSquishConstants.FLAG_HAS_COMPLEX)) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                int complexType = in.readUnsignedByte();
                switch (complexType) {
                    case NbtSquishConstants.COMPLEX_COMPOUND:
                        map.complex.add(readCompound(type, in));
                        break;
                    case NbtSquishConstants.COMPLEX_LIST:
                        map.complex.add(readNormalList(type, in));
                        break;
                    case NbtSquishConstants.COMPLEX_LIST_PACKED:
                        map.complex.add(readPackedList(type, in));
                        break;
                    default:
                        throw new IOException("Unknown complex type " + complexType);
                }
            }
        }

        return map;
    }

    /** Similar to {@link PacketBuffer#readVarInt()} */
    private static int readVarInt(DataInput in) throws IOException {
        int value = 0;
        int bytesRead = 0;

        while (true) {
            int b = in.readUnsignedByte();
            value |= (b & 0x7f) << bytesRead * 7;
            bytesRead++;
            if (bytesRead > 5) {
                throw new InvalidInputDataException("VarInt can only be up to 5 bytes long!");
            }
            if ((b & 0x80) == 0) {
                // End of number
                return value;
            }
        }
    }

    private static boolean isFlag(int flags, int flag) {
        return (flags & flag) == flag;
    }

    private NBTTagCompound readCompound(WrittenType type, DataInput in) throws IOException {
        WrittenType stringType = WrittenType.getForSize(map.stringSize());
        int count = readVarInt(in);
        NBTTagCompound nbt = new NBTTagCompound();
        for (int i = 0; i < count; i++) {
            String key = map.getStringForReading(stringType.readIndex(in));
            NBTBase value = map.getTagForReading(type.readIndex(in));
            nbt.setTag(key, value.copy());
        }
        return nbt;
    }

    private NBTTagList readNormalList(WrittenType type, DataInput in) throws IOException {
        int count = readVarInt(in);
        NBTTagList list = new NBTTagList();

        for (int i = 0; i < count; i++) {
            int index = type.readIndex(in);
            list.appendTag(map.getTagForReading(index));
        }

        return list;
    }

    private NBTTagList readPackedList(WrittenType type, DataInput in) throws IOException {
        // First make the dictionary
        int count = readVarInt(in);
        List<NBTBase> dictionary = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = type.readIndex(in);
            NBTBase nbt = map.getTagForReading(index);
            dictionary.add(nbt);
        }
        List<NBTBase> list = new ArrayList<>();
        TIntArrayList left = new TIntArrayList();
        int bits = 1;
        int entries = readVarInt(in);

        for (int i = 0; i < entries; i++) {
            list.add(null);
            left.add(i);
        }

        while (!dictionary.isEmpty()) {
            int bitsetSize = readVarInt(in);
            byte[] bitsetData = new byte[bitsetSize];
            in.readFully(bitsetData);
            DecompactingBitSet decompactor = new DecompactingBitSet(bits, bitsetData);

            TIntArrayList nextLeft = new TIntArrayList();

            int maxVal = (1 << bits) - 1;
            for (int i : left.toArray()) {
                int index = decompactor.next();
                if (index < maxVal) {
                    list.set(i, dictionary.get(index));
                } else {
                    nextLeft.add(i);
                }
            }

            dictionary.subList(0, Math.min(dictionary.size(), maxVal)).clear();
            left = nextLeft;
            bits++;
        }

        NBTTagList tag = new NBTTagList();
        for (NBTBase base : list) {
            tag.appendTag(base);
        }
        return tag;
    }
}
