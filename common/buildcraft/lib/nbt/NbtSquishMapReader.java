/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.data.NbtSquishConstants;
import buildcraft.lib.misc.data.DecompactingBitSet;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
                ByteArrayList list = new ByteArrayList();
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
                IntArrayList list = new IntArrayList();
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
                if (complexType == NbtSquishConstants.COMPLEX_COMPOUND) {
                    map.complex.add(readCompound(type, in));
                } else if (complexType == NbtSquishConstants.COMPLEX_LIST) {
                    map.complex.add(readNormalList(type, in));
                } else if (complexType == NbtSquishConstants.COMPLEX_LIST_PACKED) {
                    map.complex.add(readPackedList(type, in));
                } else {
                    throw new IOException("Unknown complex type " + complexType);
                }
            }
        }

        return map;
    }

    /** Similar to {@link FriendlyByteBuf#readVarInt()} */
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

    private CompoundTag readCompound(WrittenType type, DataInput in) throws IOException {
        WrittenType stringType = WrittenType.getForSize(map.stringSize());
        int count = readVarInt(in);
        CompoundTag nbt = new CompoundTag();
        for (int i = 0; i < count; i++) {
            String key = map.getStringForReading(stringType.readIndex(in));
            Tag value = map.getTagForReading(type.readIndex(in));
//            nbt.setTag(key, value.copy());
            nbt.put(key, value.copy());
        }
        return nbt;
    }

    private ListTag readNormalList(WrittenType type, DataInput in) throws IOException {
        int count = readVarInt(in);
        ListTag list = new ListTag();

        for (int i = 0; i < count; i++) {
            int index = type.readIndex(in);
//            list.appendTag(map.getTagForReading(index));
            list.add(map.getTagForReading(index));
        }

        return list;
    }

    private ListTag readPackedList(WrittenType type, DataInput in) throws IOException {
        // First make the dictionary
        int count = readVarInt(in);
        List<Tag> dictionary = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = type.readIndex(in);
            Tag nbt = map.getTagForReading(index);
            dictionary.add(nbt);
        }
        List<Tag> list = new ArrayList<>();
        IntArrayList left = new IntArrayList();
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

            IntArrayList nextLeft = new IntArrayList();

            int maxVal = (1 << bits) - 1;
            for (int i : left.toIntArray()) {
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

        ListTag tag = new ListTag();
        for (Tag base : list) {
//            tag.appendTag(base);
            tag.add(base);
        }
        return tag;
    }
}
