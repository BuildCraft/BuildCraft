/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.list.array.TShortArrayList;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.InvalidInputDataException;

/** Defines a map of commonly used tags. */
public class NbtSquishMap {
    // TODO: Try adding "ImmutableTagCompound" and "ImmutableTagList" to see if the equals() and hashCode() of compounds
    // is a problem atm
    // perhaps use "TCustomHashSet" with a similar deduplication functionality of FoamFix?

    // I'm not completely convinced that this one is necessary.
    // However it completes the set so, meh
    final TByteArrayList bytes = new TByteArrayList();

    final TShortArrayList shorts = new TShortArrayList();
    final TIntArrayList ints = new TIntArrayList();
    final TLongArrayList longs = new TLongArrayList();
    final TFloatArrayList floats = new TFloatArrayList();
    final TDoubleArrayList doubles = new TDoubleArrayList();

    final List<TByteArrayList> byteArrays = new ArrayList<>();
    final List<TIntArrayList> intArrays = new ArrayList<>();

    final List<String> strings = new ArrayList<>();
    final List<NbtElement> complex = new ArrayList<>();

    public NbtSquishMap() {}

    public void addTag(NbtElement nbt) {
        if (nbt instanceof NbtString) {
            String val = ((NbtString) nbt).getString();
            if (!strings.contains(val)) {
                strings.add(val);
            }
        } else if (nbt instanceof NbtByte) {
            byte val = ((NbtByte) nbt).getByte();
            if (!bytes.contains(val)) {
                bytes.add(val);
            }
        } else if (nbt instanceof NbtShort) {
            short val = ((NbtShort) nbt).getShort();
            if (!shorts.contains(val)) {
                shorts.add(val);
            }
        } else if (nbt instanceof NbtInt) {
            int val = ((NbtInt) nbt).getInt();
            if (!ints.contains(val)) {
                ints.add(val);
            }
        } else if (nbt instanceof NbtLong) {
            long val = ((NbtLong) nbt).getLong();
            if (!longs.contains(val)) {
                longs.add(val);
            }
        } else if (nbt instanceof NbtFloat) {
            float val = ((NbtFloat) nbt).getFloat();
            if (!floats.contains(val)) {
                floats.add(val);
            }
        } else if (nbt instanceof NbtDouble) {
            double val = ((NbtDouble) nbt).getDouble();
            if (!doubles.contains(val)) {
                doubles.add(val);
            }
        } else if (nbt instanceof NbtByteArray) {
            byte[] val = ((NbtByteArray) nbt).getByteArray();
            TByteArrayList array = new TByteArrayList(val);
            if (!byteArrays.contains(array)) {
                byteArrays.add(array);
            }
        } else if (nbt instanceof NbtIntArray) {
            int[] val = ((NbtIntArray) nbt).getIntArray();
            TIntArrayList array = new TIntArrayList(val);
            if (!intArrays.contains(array)) {
                intArrays.add(array);
            }
        } else if (nbt instanceof NbtList) {
            NbtList list = (NbtList) nbt;
            if (!complex.contains(list)) {
                for (int i = 0; i < list.tagCount(); i++) {
                    addTag(list.get(i));
                }
                complex.add(list);
            }
        } else if (nbt instanceof NbtCompound) {
            NbtCompound compound = (NbtCompound) nbt;
            if (!complex.contains(compound)) {
                for (String key : compound.getKeySet()) {
                    if (!strings.contains(key)) {
                        strings.add(key);
                    }
                    addTag(compound.get(key));
                }
                complex.add(compound);
            }
        } else {
            throw new IllegalArgumentException("Cannot handle tag " + nbt);
        }
    }

    public int indexOfTag(NbtElement nbt) {
        int offset = 0;
        if (nbt instanceof NbtByte) {
            return bytes.indexOf(((NbtByte) nbt).getByte());
        } else {
            offset += bytes.size();
        }
        if (nbt instanceof NbtShort) {
            return offset + shorts.indexOf(((NbtShort) nbt).getShort());
        } else {
            offset += shorts.size();
        }
        if (nbt instanceof NbtInt) {
            return offset + ints.indexOf(((NbtInt) nbt).getInt());
        } else {
            offset += ints.size();
        }
        if (nbt instanceof NbtLong) {
            return offset + longs.indexOf(((NbtLong) nbt).getLong());
        } else {
            offset += longs.size();
        }
        if (nbt instanceof NbtFloat) {
            return offset + floats.indexOf(((NbtFloat) nbt).getFloat());
        } else {
            offset += floats.size();
        }
        if (nbt instanceof NbtDouble) {
            return offset + doubles.indexOf(((NbtDouble) nbt).getDouble());
        } else {
            offset += doubles.size();
        }
        if (nbt instanceof NbtByteArray) {
            byte[] val = ((NbtByteArray) nbt).getByteArray();
            TByteArrayList array = new TByteArrayList(val);
            return offset + byteArrays.indexOf(array);
        } else {
            offset += byteArrays.size();
        }
        if (nbt instanceof NbtIntArray) {
            int[] val = ((NbtIntArray) nbt).getIntArray();
            TIntArrayList array = new TIntArrayList(val);
            return offset + intArrays.indexOf(array);
        } else {
            offset += intArrays.size();
        }
        if (nbt instanceof NbtString) {
            return offset + strings.indexOf(((NbtString) nbt).getString());
        } else {
            offset += strings.size();
        }
        if (nbt instanceof NbtList) {
            return offset + complex.indexOf(nbt);
        } else if (nbt instanceof NbtCompound) {
            return offset + complex.indexOf(nbt);
        }
        throw new IllegalArgumentException("Cannot handle tag " + nbt);
    }

    private NbtElement getTagAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index + " was less than 0!");
        }
        if (index < bytes.size()) {
            return new NbtByte(bytes.get(index));
        }
        index -= bytes.size();

        if (index < shorts.size()) {
            return new NbtShort(shorts.get(index));
        }
        index -= shorts.size();

        if (index < ints.size()) {
            return new NbtInt(ints.get(index));
        }
        index -= ints.size();

        if (index < longs.size()) {
            return new NbtLong(longs.get(index));
        }
        index -= longs.size();

        if (index < floats.size()) {
            return new NbtFloat(floats.get(index));
        }
        index -= floats.size();

        if (index < doubles.size()) {
            return new NbtDouble(doubles.get(index));
        }
        index -= doubles.size();

        if (index < byteArrays.size()) {
            return new NbtByteArray(byteArrays.get(index).toArray());
        }
        index -= byteArrays.size();

        if (index < intArrays.size()) {
            return new NbtIntArray(intArrays.get(index).toArray());
        }
        index -= intArrays.size();

        if (index < strings.size()) {
            return new NbtString(strings.get(index));
        }
        index -= strings.size();

        if (index < complex.size()) {
            return complex.get(index);
        }
        index -= complex.size();

        return null;
    }

    public NbtElement getTagForWriting(int index) {
        NbtElement value = getTagAt(index);
        if (value == null) {
            throw new IllegalArgumentException("Cannot handle index " + index);
        }
        return value;
    }

    public NbtElement getTagForReading(int index) throws IOException {
        try {
            NbtElement value = getTagAt(index);
            if (value == null) {
                throw new IOException("Cannot handle index " + index);
            }
            return value;
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidInputDataException(e);
        }
    }

    public NbtCompound getFullyReadComp(int index) throws IOException {
        NbtElement tag = getTagForReading(index);
        if (tag instanceof NbtCompound) {
            return (NbtCompound) tag;
        } else {
            throw new IOException("The tag at " + index + " was not a compound tag! (was " + tag + ")");
        }
    }

    public String getStringForReading(int index) throws IOException {
        if (index < 0 || index >= strings.size()) {
            throw new IOException("Cannot handle index " + index);
        }
        return strings.get(index);
    }

    public int sizeOf(int tagType) {
        return size(1 << (tagType - 1));
    }

    public int size(int typeFlags) {
        int total = 0;
        if (isFlag(typeFlags, NbtElement.BYTE_TYPE)) total += bytes.size();
        if (isFlag(typeFlags, NbtElement.SHORT_TYPE)) total += shorts.size();
        if (isFlag(typeFlags, NbtElement.INT_TYPE)) total += ints.size();
        if (isFlag(typeFlags, NbtElement.LONG_TYPE)) total += longs.size();
        if (isFlag(typeFlags, NbtElement.FLOAT_TYPE)) total += floats.size();
        if (isFlag(typeFlags, NbtElement.DOUBLE_TYPE)) total += doubles.size();
        if (isFlag(typeFlags, NbtElement.BYTE_TYPE_ARRAY)) total += byteArrays.size();
        if (isFlag(typeFlags, NbtElement.INT_TYPE_ARRAY)) total += intArrays.size();
        if (isFlag(typeFlags, NbtElement.STRING_TYPE)) total += strings.size();
        if (isFlag(typeFlags, NbtElement.COMPOUND_TYPE)) total += complex.size();
        else if (isFlag(typeFlags, NbtElement.LIST_TYPE)) total += complex.size();

        return total;
    }

    private static boolean isFlag(int flags, int val) {
        int flag = 1 << val;
        return (flags & flag) == flag;
    }

    public int size() {
        return size(-1);
    }

    public WrittenType getWrittenType() {
        return WrittenType.getForSize(size());
    }

    public int stringSize() {
        return strings.size();
    }
}
