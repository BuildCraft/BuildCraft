/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

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

import buildcraft.api.data.NBTSquishConstants;

import buildcraft.lib.misc.data.DecompactingBitSet;

class NBTSquishMapReader {
    private final NBTSquishMap map = new NBTSquishMap();

    public static NBTSquishMap read(PacketBuffer buf) throws IOException {
        return new NBTSquishMapReader().readInternal(buf);
    }

    private NBTSquishMap readInternal(PacketBuffer buf) throws IOException {
        WrittenType type = WrittenType.readType(buf);
        int flags = buf.readInt();

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_BYTES)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                map.bytes.add(buf.readByte());
            }
        }

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_SHORTS)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                map.shorts.add(buf.readShort());
            }
        }

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_INTS)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                map.ints.add(buf.readInt());
            }
        }

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_LONGS)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                map.longs.add(buf.readLong());
            }
        }

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_FLOATS)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                map.floats.add(buf.readFloat());
            }
        }

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_DOUBLES)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                map.doubles.add(buf.readDouble());
            }
        }

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_BYTE_ARRAYS)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                int arraySize = buf.readUnsignedShort();
                TByteArrayList list = new TByteArrayList();
                for (int j = 0; j < arraySize; j++) {
                    list.add(buf.readByte());
                }
                map.byteArrays.add(list);
            }
        }

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_INT_ARRAYS)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                int arraySize = buf.readUnsignedShort();
                TIntArrayList list = new TIntArrayList();
                for (int j = 0; j < arraySize; j++) {
                    list.add(buf.readInt());
                }
                map.intArrays.add(list);
            }
        }

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_STRINGS)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                int length = buf.readUnsignedShort();
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                map.strings.add(new String(bytes, StandardCharsets.UTF_8));
            }
        }

        if (isFlag(flags, NBTSquishConstants.FLAG_HAS_COMPLEX)) {
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                int complexType = buf.readUnsignedByte();
                if (complexType == NBTSquishConstants.COMPLEX_COMPOUND) {
                    map.complex.add(readCompound(type, buf));
                } else if (complexType == NBTSquishConstants.COMPLEX_LIST) {
                    map.complex.add(readNormalList(type, buf));
                } else if (complexType == NBTSquishConstants.COMPLEX_LIST_PACKED) {
                    map.complex.add(readPackedList(type, buf));
                } else {
                    throw new IOException("Unknown complex type " + complexType);
                }
            }
        }

        return map;
    }

    private static boolean isFlag(int flags, int flag) {
        return (flags & flag) == flag;
    }

    private NBTTagCompound readCompound(WrittenType type, PacketBuffer buf) throws IOException {
        WrittenType stringType = WrittenType.getForSize(map.stringSize());
        int count = buf.readVarInt();
        NBTTagCompound nbt = new NBTTagCompound();
        for (int i = 0; i < count; i++) {
            String key = map.getStringForReading(stringType.readIndex(buf));
            NBTBase value = map.getTagForReading(type.readIndex(buf));
            nbt.setTag(key, value.copy());
        }
        return nbt;
    }

    private NBTTagList readNormalList(WrittenType type, PacketBuffer buf) throws IOException {
        int count = buf.readVarInt();
        NBTTagList list = new NBTTagList();

        for (int i = 0; i < count; i++) {
            int index = type.readIndex(buf);
            list.appendTag(map.getTagForReading(index));
        }

        return list;
    }

    private NBTTagList readPackedList(WrittenType type, PacketBuffer buf) throws IOException {
        // First make the dictionary
        int count = buf.readVarInt();
        List<NBTBase> dictionary = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = type.readIndex(buf);
            NBTBase nbt = map.getTagForReading(index);
            dictionary.add(nbt);
        }
        List<NBTBase> list = new ArrayList<>();
        TIntArrayList left = new TIntArrayList();
        int bits = 1;
        int entries = buf.readVarInt();

        for (int i = 0; i < entries; i++) {
            list.add(null);
            left.add(i);
        }

        while (!dictionary.isEmpty()) {
            int bitsetSize = buf.readVarInt();
            byte[] bitsetData = new byte[bitsetSize];
            buf.readBytes(bitsetData);
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
