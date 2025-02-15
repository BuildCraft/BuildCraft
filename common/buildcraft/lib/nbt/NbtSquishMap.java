/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import buildcraft.api.core.InvalidInputDataException;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import net.minecraft.nbt.*;

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
    final ByteArrayList bytes = new ByteArrayList();

    final ShortArrayList shorts = new ShortArrayList();
    final IntArrayList ints = new IntArrayList();
    final LongArrayList longs = new LongArrayList();
    final FloatArrayList floats = new FloatArrayList();
    final DoubleArrayList doubles = new DoubleArrayList();

    final List<ByteArrayList> byteArrays = new ArrayList<>();
    final List<IntArrayList> intArrays = new ArrayList<>();

    final List<String> strings = new ArrayList<>();
    final List<Tag> complex = new ArrayList<>();

    public NbtSquishMap() {
    }

    public void addTag(Tag nbt) {
        if (nbt instanceof StringTag) {
//            String val = ((StringTag) nbt).getString();
            String val = ((StringTag) nbt).getAsString();
            if (!strings.contains(val)) {
                strings.add(val);
            }
        } else if (nbt instanceof ByteTag) {
            byte val = ((ByteTag) nbt).getAsByte();
            if (!bytes.contains(val)) {
                bytes.add(val);
            }
        } else if (nbt instanceof ShortTag) {
            short val = ((ShortTag) nbt).getAsShort();
            if (!shorts.contains(val)) {
                shorts.add(val);
            }
        } else if (nbt instanceof IntTag) {
            int val = ((IntTag) nbt).getAsInt();
            if (!ints.contains(val)) {
                ints.add(val);
            }
        } else if (nbt instanceof LongTag) {
            long val = ((LongTag) nbt).getAsLong();
            if (!longs.contains(val)) {
                longs.add(val);
            }
        } else if (nbt instanceof FloatTag) {
            float val = ((FloatTag) nbt).getAsFloat();
            if (!floats.contains(val)) {
                floats.add(val);
            }
        } else if (nbt instanceof DoubleTag) {
            double val = ((DoubleTag) nbt).getAsDouble();
            if (!doubles.contains(val)) {
                doubles.add(val);
            }
        } else if (nbt instanceof ByteArrayTag) {
            byte[] val = ((ByteArrayTag) nbt).getAsByteArray();
            ByteArrayList array = new ByteArrayList(val);
            if (!byteArrays.contains(array)) {
                byteArrays.add(array);
            }
        } else if (nbt instanceof IntArrayTag) {
            int[] val = ((IntArrayTag) nbt).getAsIntArray();
            IntArrayList array = new IntArrayList(val);
            if (!intArrays.contains(array)) {
                intArrays.add(array);
            }
        } else if (nbt instanceof ListTag) {
            ListTag list = (ListTag) nbt;
            if (!complex.contains(list)) {
//                for (int i = 0; i < list.tagCount(); i++)
                for (int i = 0; i < list.size(); i++) {
                    addTag(list.get(i));
                }
                complex.add(list);
            }
        } else if (nbt instanceof CompoundTag) {
            CompoundTag compound = (CompoundTag) nbt;
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

    public int indexOfTag(Tag nbt) {
        int offset = 0;
        if (nbt instanceof ByteTag) {
//            return bytes.indexOf(((ByteTag) nbt).getByte());
            return bytes.indexOf(((ByteTag) nbt).getAsByte());
        } else {
            offset += bytes.size();
        }
        if (nbt instanceof ShortTag) {
            return offset + shorts.indexOf(((ShortTag) nbt).getAsShort());
        } else {
            offset += shorts.size();
        }
        if (nbt instanceof IntTag) {
            return offset + ints.indexOf(((IntTag) nbt).getAsInt());
        } else {
            offset += ints.size();
        }
        if (nbt instanceof LongTag) {
            return offset + longs.indexOf(((LongTag) nbt).getAsLong());
        } else {
            offset += longs.size();
        }
        if (nbt instanceof FloatTag) {
            return offset + floats.indexOf(((FloatTag) nbt).getAsFloat());
        } else {
            offset += floats.size();
        }
        if (nbt instanceof DoubleTag) {
            return offset + doubles.indexOf(((DoubleTag) nbt).getAsDouble());
        } else {
            offset += doubles.size();
        }
        if (nbt instanceof ByteArrayTag) {
            byte[] val = ((ByteArrayTag) nbt).getAsByteArray();
            ByteArrayList array = new ByteArrayList(val);
            return offset + byteArrays.indexOf(array);
        } else {
            offset += byteArrays.size();
        }
        if (nbt instanceof IntArrayTag) {
            int[] val = ((IntArrayTag) nbt).getAsIntArray();
            IntArrayList array = new IntArrayList(val);
            return offset + intArrays.indexOf(array);
        } else {
            offset += intArrays.size();
        }
        if (nbt instanceof StringTag) {
//            return offset + strings.indexOf(((StringTag) nbt).getString());
            return offset + strings.indexOf(((StringTag) nbt).getAsString());
        } else {
            offset += strings.size();
        }
        if (nbt instanceof ListTag) {
            return offset + complex.indexOf(nbt);
        } else if (nbt instanceof CompoundTag) {
            return offset + complex.indexOf(nbt);
        }
        throw new IllegalArgumentException("Cannot handle tag " + nbt);
    }

    private Tag getTagAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index + " was less than 0!");
        }
        if (index < bytes.size()) {
//            return new ByteTag(bytes.get(index));
            return ByteTag.valueOf(bytes.get(index));
        }
        index -= bytes.size();

        if (index < shorts.size()) {
//            return new ShortTag(shorts.get(index));
            return ShortTag.valueOf(shorts.get(index));
        }
        index -= shorts.size();

        if (index < ints.size()) {
//            return new IntTag(ints.get(index));
            return IntTag.valueOf(ints.get(index));
        }
        index -= ints.size();

        if (index < longs.size()) {
//            return new LongTag(longs.get(index));
            return LongTag.valueOf(longs.get(index));
        }
        index -= longs.size();

        if (index < floats.size()) {
//            return new FloatTag(floats.get(index));
            return FloatTag.valueOf(floats.get(index));
        }
        index -= floats.size();

        if (index < doubles.size()) {
//            return new DoubleTag(doubles.get(index));
            return DoubleTag.valueOf(doubles.get(index));
        }
        index -= doubles.size();

        if (index < byteArrays.size()) {
            return new ByteArrayTag(byteArrays.get(index).toByteArray());
        }
        index -= byteArrays.size();

        if (index < intArrays.size()) {
            return new IntArrayTag(intArrays.get(index).toIntArray());
        }
        index -= intArrays.size();

        if (index < strings.size()) {
            return StringTag.valueOf(strings.get(index));
        }
        index -= strings.size();

        if (index < complex.size()) {
            return complex.get(index);
        }
        index -= complex.size();

        return null;
    }

    public Tag getTagForWriting(int index) {
        Tag value = getTagAt(index);
        if (value == null) {
            throw new IllegalArgumentException("Cannot handle index " + index);
        }
        return value;
    }

    public Tag getTagForReading(int index) throws IOException {
        try {
            Tag value = getTagAt(index);
            if (value == null) {
                throw new IOException("Cannot handle index " + index);
            }
            return value;
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidInputDataException(e);
        }
    }

    public CompoundTag getFullyReadComp(int index) throws IOException {
        Tag tag = getTagForReading(index);
        if (tag instanceof CompoundTag) {
            return (CompoundTag) tag;
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
        if (isFlag(typeFlags, Tag.TAG_BYTE)) total += bytes.size();
        if (isFlag(typeFlags, Tag.TAG_SHORT)) total += shorts.size();
        if (isFlag(typeFlags, Tag.TAG_INT)) total += ints.size();
        if (isFlag(typeFlags, Tag.TAG_LONG)) total += longs.size();
        if (isFlag(typeFlags, Tag.TAG_FLOAT)) total += floats.size();
        if (isFlag(typeFlags, Tag.TAG_DOUBLE)) total += doubles.size();
        if (isFlag(typeFlags, Tag.TAG_BYTE_ARRAY)) total += byteArrays.size();
        if (isFlag(typeFlags, Tag.TAG_INT_ARRAY)) total += intArrays.size();
        if (isFlag(typeFlags, Tag.TAG_STRING)) total += strings.size();
        if (isFlag(typeFlags, Tag.TAG_COMPOUND)) total += complex.size();
        else if (isFlag(typeFlags, Tag.TAG_LIST)) total += complex.size();

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
