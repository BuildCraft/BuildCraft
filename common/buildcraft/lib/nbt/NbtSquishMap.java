/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;


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
    final ArrayList<Byte> bytes = new ArrayList<Byte>();

    final ArrayList<Short> shorts = new ArrayList<Short>();
    final ArrayList<Integer> ints = new ArrayList<Integer>();
    final ArrayList<Long> longs = new ArrayList<Long>();
    final ArrayList<Float> floats = new ArrayList<Float>();
    final ArrayList<Double> doubles = new ArrayList<Double>();

    final List<ArrayList<Byte>> byteArrays = new ArrayList<>();
    final List<ArrayList<Integer>> intArrays = new ArrayList<>();

    final List<String> strings = new ArrayList<>();
    final List<NbtElement> complex = new ArrayList<>();

    public NbtSquishMap() {}

    public void addTag(NbtElement nbt) {
        if (nbt instanceof NbtString) {
            String val = ((NbtString)nbt).asString();
            if (!strings.contains(val)) {
                strings.add(val);
            }
        } else if (nbt instanceof NbtByte) {
            byte val = ((nbt) instanceof NbtByte ? ((NbtByte)nbt).byteValue() : 0);
            if (!bytes.contains(val)) {
                bytes.add(val);
            }
        } else if (nbt instanceof NbtShort) {
            short val = ((NbtShort)nbt).shortValue();
            if (!shorts.contains(val)) {
                shorts.add(val);
            }
        } else if (nbt instanceof NbtInt) {
            int val = ((NbtInt)nbt).intValue();
            if (!ints.contains(val)) {
                ints.add(val);
            }
        } else if (nbt instanceof NbtLong) {
            long val = ((NbtLong)nbt).longValue();
            if (!longs.contains(val)) {
                longs.add(val);
            }
        } else if (nbt instanceof NbtFloat) {
            float val = ((NbtFloat)nbt).floatValue();
            if (!floats.contains(val)) {
                floats.add(val);
            }
        } else if (nbt instanceof NbtDouble) {
            double val = ((NbtDouble)nbt).doubleValue();
            if (!doubles.contains(val)) {
                doubles.add(val);
            }
        } else if (nbt instanceof NbtByteArray) {
            byte[] val = ((NbtByteArray) nbt).getByteArray();
            ArrayList<Byte> array = new ArrayList<>(); for (byte b : val) array.add(b);
            if (!byteArrays.contains(array)) {
                byteArrays.add(array);
            }
        } else if (nbt instanceof NbtIntArray) {
            int[] val = ((NbtIntArray) nbt).getIntArray();
            ArrayList<Integer> array = new ArrayList<>(); for (int i : val) array.add(i);
            if (!intArrays.contains(array)) {
                intArrays.add(array);
            }
        } else if (nbt instanceof NbtList) {
            NbtList list = (NbtList) nbt;
            if (!complex.contains(list)) {
                for (int i = 0; i < list.size(); i++) {
                    addTag(list.get(i));
                }
                complex.add(list);
            }
        } else if (nbt instanceof NbtCompound) {
            NbtCompound compound = (NbtCompound) nbt;
            if (!complex.contains(compound)) {
                for (String key : compound.getKeys()) {
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
            return bytes.indexOf(((nbt) instanceof NbtByte ? ((NbtByte)nbt).byteValue() : 0));
        } else {
            offset += bytes.size();
        }
        if (nbt instanceof NbtShort) {
            return offset + shorts.indexOf(((NbtShort)nbt).shortValue());
        } else {
            offset += shorts.size();
        }
        if (nbt instanceof NbtInt) {
            return offset + ints.indexOf(((NbtInt)nbt).intValue());
        } else {
            offset += ints.size();
        }
        if (nbt instanceof NbtLong) {
            return offset + longs.indexOf(((NbtLong)nbt).longValue());
        } else {
            offset += longs.size();
        }
        if (nbt instanceof NbtFloat) {
            return offset + floats.indexOf(((NbtFloat)nbt).floatValue());
        } else {
            offset += floats.size();
        }
        if (nbt instanceof NbtDouble) {
            return offset + doubles.indexOf(((NbtDouble)nbt).doubleValue());
        } else {
            offset += doubles.size();
        }
        if (nbt instanceof NbtByteArray) {
            byte[] val = ((NbtByteArray) nbt).getByteArray();
            ArrayList<Byte> array = new ArrayList<>(); for (byte b : val) array.add(b);
            return offset + byteArrays.indexOf(array);
        } else {
            offset += byteArrays.size();
        }
        if (nbt instanceof NbtIntArray) {
            int[] val = ((NbtIntArray) nbt).getIntArray();
            ArrayList<Integer> array = new ArrayList<>(); for (int i : val) array.add(i);
            return offset + intArrays.indexOf(array);
        } else {
            offset += intArrays.size();
        }
        if (nbt instanceof NbtString) {
            return offset + strings.indexOf(((NbtString)nbt).asString());
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
            return NbtByte.of(bytes.get(index));
        }
        index -= bytes.size();

        if (index < shorts.size()) {
            return NbtShort.of(shorts.get(index));
        }
        index -= shorts.size();

        if (index < ints.size()) {
            return NbtInt.of(ints.get(index));
        }
        index -= ints.size();

        if (index < longs.size()) {
            return NbtLong.of(longs.get(index));
        }
        index -= longs.size();

        if (index < floats.size()) {
            return NbtFloat.of(floats.get(index));
        }
        index -= floats.size();

        if (index < doubles.size()) {
            return NbtDouble.of(doubles.get(index));
        }
        index -= doubles.size();

        if (index < byteArrays.size()) {
            ArrayList<Byte> src = byteArrays.get(index);
            byte[] arr = new byte[src.size()];
            for (int i = 0; i < src.size(); i++) arr[i] = src.get(i);
            return new NbtByteArray(arr);
        }
        index -= byteArrays.size();

        if (index < intArrays.size()) {
            return new NbtIntArray(intArrays.get(index).stream().mapToInt(Integer::intValue).toArray());
        }
        index -= intArrays.size();

        if (index < strings.size()) {
            return NbtString.of(strings.get(index));
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
        if (isFlag(typeFlags, NbtElement.BYTE_ARRAY_TYPE)) total += byteArrays.size();
        if (isFlag(typeFlags, NbtElement.INT_ARRAY_TYPE)) total += intArrays.size();
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
