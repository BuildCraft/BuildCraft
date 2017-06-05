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

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.InvalidInputDataException;

/** Defines a map of commonly used tags. */
public class NbtSquishMap {
    // TODO: Try adding "ImmutableTagCompound" and "ImmutableTagList" to see if the equals() and hashCode() of compounds
    // is a problem atm
    // perhaps use "TCustomHashSet" with a simalir deduplicating functionality of FoamFix?

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
    final List<NBTBase> complex = new ArrayList<>();

    public NbtSquishMap() {}

    public void addTag(NBTBase nbt) {
        if (nbt instanceof NBTTagString) {
            String val = ((NBTTagString) nbt).getString();
            if (!strings.contains(val)) {
                strings.add(val);
            }
        } else if (nbt instanceof NBTTagByte) {
            byte val = ((NBTTagByte) nbt).getByte();
            if (!bytes.contains(val)) {
                bytes.add(val);
            }
        } else if (nbt instanceof NBTTagShort) {
            short val = ((NBTTagShort) nbt).getShort();
            if (!shorts.contains(val)) {
                shorts.add(val);
            }
        } else if (nbt instanceof NBTTagInt) {
            int val = ((NBTTagInt) nbt).getInt();
            if (!ints.contains(val)) {
                ints.add(val);
            }
        } else if (nbt instanceof NBTTagLong) {
            long val = ((NBTTagLong) nbt).getLong();
            if (!longs.contains(val)) {
                longs.add(val);
            }
        } else if (nbt instanceof NBTTagFloat) {
            float val = ((NBTTagFloat) nbt).getFloat();
            if (!floats.contains(val)) {
                floats.add(val);
            }
        } else if (nbt instanceof NBTTagDouble) {
            double val = ((NBTTagDouble) nbt).getDouble();
            if (!doubles.contains(val)) {
                doubles.add(val);
            }
        } else if (nbt instanceof NBTTagByteArray) {
            byte[] val = ((NBTTagByteArray) nbt).getByteArray();
            TByteArrayList array = new TByteArrayList(val);
            if (!byteArrays.contains(array)) {
                byteArrays.add(array);
            }
        } else if (nbt instanceof NBTTagIntArray) {
            int[] val = ((NBTTagIntArray) nbt).getIntArray();
            TIntArrayList array = new TIntArrayList(val);
            if (!intArrays.contains(array)) {
                intArrays.add(array);
            }
        } else if (nbt instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) nbt;
            if (!complex.contains(list)) {
                for (int i = 0; i < list.tagCount(); i++) {
                    addTag(list.get(i));
                }
                complex.add(list);
            }
        } else if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            if (!complex.contains(compound)) {
                for (String key : compound.getKeySet()) {
                    if (!strings.contains(key)) {
                        strings.add(key);
                    }
                    addTag(compound.getTag(key));
                }
                complex.add(compound);
            }
        } else {
            throw new IllegalArgumentException("Cannot handle tag " + nbt);
        }
    }

    public int indexOfTag(NBTBase nbt) {
        int offset = 0;
        if (nbt instanceof NBTTagByte) {
            return bytes.indexOf(((NBTTagByte) nbt).getByte());
        } else {
            offset += bytes.size();
        }
        if (nbt instanceof NBTTagShort) {
            return offset + shorts.indexOf(((NBTTagShort) nbt).getShort());
        } else {
            offset += shorts.size();
        }
        if (nbt instanceof NBTTagInt) {
            return offset + ints.indexOf(((NBTTagInt) nbt).getInt());
        } else {
            offset += ints.size();
        }
        if (nbt instanceof NBTTagLong) {
            return offset + longs.indexOf(((NBTTagLong) nbt).getLong());
        } else {
            offset += longs.size();
        }
        if (nbt instanceof NBTTagFloat) {
            return offset + floats.indexOf(((NBTTagFloat) nbt).getFloat());
        } else {
            offset += floats.size();
        }
        if (nbt instanceof NBTTagDouble) {
            return offset + doubles.indexOf(((NBTTagDouble) nbt).getDouble());
        } else {
            offset += doubles.size();
        }
        if (nbt instanceof NBTTagByteArray) {
            byte[] val = ((NBTTagByteArray) nbt).getByteArray();
            TByteArrayList array = new TByteArrayList(val);
            return offset + byteArrays.indexOf(array);
        } else {
            offset += byteArrays.size();
        }
        if (nbt instanceof NBTTagIntArray) {
            int[] val = ((NBTTagIntArray) nbt).getIntArray();
            TIntArrayList array = new TIntArrayList(val);
            return offset + intArrays.indexOf(array);
        } else {
            offset += intArrays.size();
        }
        if (nbt instanceof NBTTagString) {
            return offset + strings.indexOf(((NBTTagString) nbt).getString());
        } else {
            offset += strings.size();
        }
        if (nbt instanceof NBTTagList) {
            return offset + complex.indexOf(nbt);
        } else if (nbt instanceof NBTTagCompound) {
            return offset + complex.indexOf(nbt);
        }
        throw new IllegalArgumentException("Cannot handle tag " + nbt);
    }

    private NBTBase getTagAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index + " was less than 0!");
        }
        if (index < bytes.size()) {
            return new NBTTagByte(bytes.get(index));
        }
        index -= bytes.size();

        if (index < shorts.size()) {
            return new NBTTagShort(shorts.get(index));
        }
        index -= shorts.size();

        if (index < ints.size()) {
            return new NBTTagInt(ints.get(index));
        }
        index -= ints.size();

        if (index < longs.size()) {
            return new NBTTagLong(longs.get(index));
        }
        index -= longs.size();

        if (index < floats.size()) {
            return new NBTTagFloat(floats.get(index));
        }
        index -= floats.size();

        if (index < doubles.size()) {
            return new NBTTagDouble(doubles.get(index));
        }
        index -= doubles.size();

        if (index < byteArrays.size()) {
            return new NBTTagByteArray(byteArrays.get(index).toArray());
        }
        index -= byteArrays.size();

        if (index < intArrays.size()) {
            return new NBTTagIntArray(intArrays.get(index).toArray());
        }
        index -= intArrays.size();

        if (index < strings.size()) {
            return new NBTTagString(strings.get(index));
        }
        index -= strings.size();

        if (index < complex.size()) {
            return complex.get(index);
        }
        index -= complex.size();

        return null;
    }

    public NBTBase getTagForWriting(int index) {
        NBTBase value = getTagAt(index);
        if (value == null) {
            throw new IllegalArgumentException("Cannot handle index " + index);
        }
        return value;
    }

    public NBTBase getTagForReading(int index) throws IOException {
        try {
            NBTBase value = getTagAt(index);
            if (value == null) {
                throw new IOException("Cannot handle index " + index);
            }
            return value;
        } catch (IndexOutOfBoundsException ioobe) {
            throw new InvalidInputDataException(ioobe);
        }
    }

    public NBTTagCompound getFullyReadComp(int index) throws IOException {
        NBTBase tag = getTagForReading(index);
        if (tag instanceof NBTTagCompound) {
            return (NBTTagCompound) tag;
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
        if (isFlag(typeFlags, Constants.NBT.TAG_BYTE)) total += bytes.size();
        if (isFlag(typeFlags, Constants.NBT.TAG_SHORT)) total += shorts.size();
        if (isFlag(typeFlags, Constants.NBT.TAG_INT)) total += ints.size();
        if (isFlag(typeFlags, Constants.NBT.TAG_LONG)) total += longs.size();
        if (isFlag(typeFlags, Constants.NBT.TAG_FLOAT)) total += floats.size();
        if (isFlag(typeFlags, Constants.NBT.TAG_DOUBLE)) total += doubles.size();
        if (isFlag(typeFlags, Constants.NBT.TAG_BYTE_ARRAY)) total += byteArrays.size();
        if (isFlag(typeFlags, Constants.NBT.TAG_INT_ARRAY)) total += intArrays.size();
        if (isFlag(typeFlags, Constants.NBT.TAG_STRING)) total += strings.size();
        if (isFlag(typeFlags, Constants.NBT.TAG_COMPOUND)) total += complex.size();
        else if (isFlag(typeFlags, Constants.NBT.TAG_LIST)) total += complex.size();

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
