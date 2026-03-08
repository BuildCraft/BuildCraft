/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import buildcraft.api.core.InvalidInputDataException;
import gnu.trove.list.array.*;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    final List<INBT> complex = new ArrayList<>();

    public NbtSquishMap() {
    }

    public void addTag(INBT nbt) {
        if (nbt instanceof StringNBT) {
//            String val = ((StringTag) nbt).getString();
            String val = ((StringNBT) nbt).getAsString();
            if (!strings.contains(val)) {
                strings.add(val);
            }
        } else if (nbt instanceof ByteNBT) {
            byte val = ((ByteNBT) nbt).getAsByte();
            if (!bytes.contains(val)) {
                bytes.add(val);
            }
        } else if (nbt instanceof ShortNBT) {
            short val = ((ShortNBT) nbt).getAsShort();
            if (!shorts.contains(val)) {
                shorts.add(val);
            }
        } else if (nbt instanceof IntNBT) {
            int val = ((IntNBT) nbt).getAsInt();
            if (!ints.contains(val)) {
                ints.add(val);
            }
        } else if (nbt instanceof LongNBT) {
            long val = ((LongNBT) nbt).getAsLong();
            if (!longs.contains(val)) {
                longs.add(val);
            }
        } else if (nbt instanceof FloatNBT) {
            float val = ((FloatNBT) nbt).getAsFloat();
            if (!floats.contains(val)) {
                floats.add(val);
            }
        } else if (nbt instanceof DoubleNBT) {
            double val = ((DoubleNBT) nbt).getAsDouble();
            if (!doubles.contains(val)) {
                doubles.add(val);
            }
        } else if (nbt instanceof ByteArrayNBT) {
            byte[] val = ((ByteArrayNBT) nbt).getAsByteArray();
            TByteArrayList array = new TByteArrayList(val);
            if (!byteArrays.contains(array)) {
                byteArrays.add(array);
            }
        } else if (nbt instanceof IntArrayNBT) {
            int[] val = ((IntArrayNBT) nbt).getAsIntArray();
            TIntArrayList array = new TIntArrayList(val);
            if (!intArrays.contains(array)) {
                intArrays.add(array);
            }
        } else if (nbt instanceof ListNBT) {
            ListNBT list = (ListNBT) nbt;
            if (!complex.contains(list)) {
//                for (int i = 0; i < list.tagCount(); i++)
                for (int i = 0; i < list.size(); i++) {
                    addTag(list.get(i));
                }
                complex.add(list);
            }
        } else if (nbt instanceof CompoundNBT) {
            CompoundNBT compound = (CompoundNBT) nbt;
            if (!complex.contains(compound)) {
                for (String key : compound.getAllKeys()) {
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

    public int indexOfTag(INBT nbt) {
        int offset = 0;
        if (nbt instanceof ByteNBT) {
//            return bytes.indexOf(((ByteTag) nbt).getByte());
            return bytes.indexOf(((ByteNBT) nbt).getAsByte());
        } else {
            offset += bytes.size();
        }
        if (nbt instanceof ShortNBT) {
            return offset + shorts.indexOf(((ShortNBT) nbt).getAsShort());
        } else {
            offset += shorts.size();
        }
        if (nbt instanceof IntNBT) {
            return offset + ints.indexOf(((IntNBT) nbt).getAsInt());
        } else {
            offset += ints.size();
        }
        if (nbt instanceof LongNBT) {
            return offset + longs.indexOf(((LongNBT) nbt).getAsLong());
        } else {
            offset += longs.size();
        }
        if (nbt instanceof FloatNBT) {
            return offset + floats.indexOf(((FloatNBT) nbt).getAsFloat());
        } else {
            offset += floats.size();
        }
        if (nbt instanceof DoubleNBT) {
            return offset + doubles.indexOf(((DoubleNBT) nbt).getAsDouble());
        } else {
            offset += doubles.size();
        }
        if (nbt instanceof ByteArrayNBT) {
            byte[] val = ((ByteArrayNBT) nbt).getAsByteArray();
            TByteArrayList array = new TByteArrayList(val);
            return offset + byteArrays.indexOf(array);
        } else {
            offset += byteArrays.size();
        }
        if (nbt instanceof IntArrayNBT) {
            int[] val = ((IntArrayNBT) nbt).getAsIntArray();
            TIntArrayList array = new TIntArrayList(val);
            return offset + intArrays.indexOf(array);
        } else {
            offset += intArrays.size();
        }
        if (nbt instanceof StringNBT) {
//            return offset + strings.indexOf(((StringTag) nbt).getString());
            return offset + strings.indexOf(((StringNBT) nbt).getAsString());
        } else {
            offset += strings.size();
        }
        if (nbt instanceof ListNBT) {
            return offset + complex.indexOf(nbt);
        } else if (nbt instanceof CompoundNBT) {
            return offset + complex.indexOf(nbt);
        }
        throw new IllegalArgumentException("Cannot handle tag " + nbt);
    }

    private INBT getTagAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index + " was less than 0!");
        }
        if (index < bytes.size()) {
//            return new ByteTag(bytes.get(index));
            return ByteNBT.valueOf(bytes.get(index));
        }
        index -= bytes.size();

        if (index < shorts.size()) {
//            return new ShortTag(shorts.get(index));
            return ShortNBT.valueOf(shorts.get(index));
        }
        index -= shorts.size();

        if (index < ints.size()) {
//            return new IntTag(ints.get(index));
            return IntNBT.valueOf(ints.get(index));
        }
        index -= ints.size();

        if (index < longs.size()) {
//            return new LongTag(longs.get(index));
            return LongNBT.valueOf(longs.get(index));
        }
        index -= longs.size();

        if (index < floats.size()) {
//            return new FloatTag(floats.get(index));
            return FloatNBT.valueOf(floats.get(index));
        }
        index -= floats.size();

        if (index < doubles.size()) {
//            return new DoubleTag(doubles.get(index));
            return DoubleNBT.valueOf(doubles.get(index));
        }
        index -= doubles.size();

        if (index < byteArrays.size()) {
            return new ByteArrayNBT(byteArrays.get(index).toArray());
        }
        index -= byteArrays.size();

        if (index < intArrays.size()) {
            return new IntArrayNBT(intArrays.get(index).toArray());
        }
        index -= intArrays.size();

        if (index < strings.size()) {
            return StringNBT.valueOf(strings.get(index));
        }
        index -= strings.size();

        if (index < complex.size()) {
            return complex.get(index);
        }
        index -= complex.size();

        return null;
    }

    public INBT getTagForWriting(int index) {
        INBT value = getTagAt(index);
        if (value == null) {
            throw new IllegalArgumentException("Cannot handle index " + index);
        }
        return value;
    }

    public INBT getTagForReading(int index) throws IOException {
        try {
            INBT value = getTagAt(index);
            if (value == null) {
                throw new IOException("Cannot handle index " + index);
            }
            return value;
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidInputDataException(e);
        }
    }

    public CompoundNBT getFullyReadComp(int index) throws IOException {
        INBT tag = getTagForReading(index);
        if (tag instanceof CompoundNBT) {
            return (CompoundNBT) tag;
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
