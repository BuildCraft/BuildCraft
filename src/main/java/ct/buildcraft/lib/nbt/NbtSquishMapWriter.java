/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.nbt;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ct.buildcraft.api.data.NbtSquishConstants;
import ct.buildcraft.lib.misc.data.CompactingBitSet;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteComparators;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatComparators;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortComparators;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

class NbtSquishMapWriter {
    static boolean debug;
    static final boolean sort = true;
    static final Boolean packList = null;
//    static final Profiler profiler = NbtSquisher.profiler;
    private final NbtSquishMap map;
    
    private static void log(String string) {
        if (debug) {
//            Bootstrap.SYSOUT.print(string + "\n");
        } else {
            throw new IllegalArgumentException("Don't allocate a string if we aren't debugging!");
        }
    }

    public NbtSquishMapWriter(NbtSquishMap map) {
        this.map = map;
    }

    public static void write(NbtSquishMap map, DataOutput to) throws IOException {
        new NbtSquishMapWriter(map).write(to);
    }

    private void write(DataOutput to) throws IOException {
//        profiler.startSection("write");
//        profiler.startSection("flags");
        WrittenType type = map.getWrittenType();

        type.writeType(to);

        ByteArrayList bytes = map.bytes;
        ShortArrayList shorts = map.shorts;
        IntArrayList ints = map.ints;
        LongArrayList longs = map.longs;
        FloatArrayList floats = map.floats;
        DoubleArrayList doubles = map.doubles;
        List<ByteArrayList> byteArrays = map.byteArrays;
        List<IntArrayList> intArrays = map.intArrays;
        List<String> strings = map.strings;
        List<Tag> complex = map.complex;

        int flags = 0;
        if (!bytes.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_BYTES;
        if (!shorts.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_SHORTS;
        if (!ints.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_INTS;
        if (!longs.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_LONGS;
        if (!floats.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_FLOATS;
        if (!doubles.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_DOUBLES;
        if (!byteArrays.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_BYTE_ARRAYS;
        if (!intArrays.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_INT_ARRAYS;
        if (!strings.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_STRINGS;
        if (!complex.isEmpty()) flags |= NbtSquishConstants.FLAG_HAS_COMPLEX;

        if (debug) log("\nUsed flags = " + Integer.toBinaryString(flags));
        to.writeInt(flags);

//        profiler.endStartSection("bytes");
        if (!bytes.isEmpty()) {
            if (debug) log("\nByte dictionary size = " + bytes.size());
            if (sort) bytes.sort(ByteComparators.NATURAL_COMPARATOR);
            writeVarInt(to, bytes.size());
            for (byte b : bytes) {
                to.writeByte(b);
            }
        }
//        profiler.endStartSection("shorts");
        if (!shorts.isEmpty()) {
            if (debug) log("\nShort dictionary size = " + shorts.size());
            if (sort) shorts.sort(ShortComparators.NATURAL_COMPARATOR);
            writeVarInt(to, shorts.size());
            for (short s : shorts) {
                to.writeShort(s);
            }
        }
//        profiler.endStartSection("integers");
        if (!ints.isEmpty()) {
            if (debug) log("\nInt dictionary size = " + ints.size());
            if (sort) ints.sort(IntComparators.NATURAL_COMPARATOR);
            writeVarInt(to, ints.size());
            for (int i : ints) {
                to.writeInt(i);
            }
        }
//        profiler.endStartSection("longs");
        if (!longs.isEmpty()) {
            if (debug) log("\nLong dictionary size = " + longs.size());
            if (sort) longs.sort(LongComparators.NATURAL_COMPARATOR);
            writeVarInt(to, longs.size());
            for (long l : longs) {
                to.writeLong(l);
            }
        }
//        profiler.endStartSection("floats");
        if (!floats.isEmpty()) {
            if (debug) log("\nFloat dictionary size = " + floats.size());
            if (sort) floats.sort(FloatComparators.NATURAL_COMPARATOR);
            writeVarInt(to, floats.size());
            for (float f : floats) {
                to.writeFloat(f);
            }
        }
//        profiler.endStartSection("doubles");
        if (!doubles.isEmpty()) {
            if (debug) log("\nDouble dictionary size = " + doubles.size());
            if (sort) doubles.sort(DoubleComparators.NATURAL_COMPARATOR);
            writeVarInt(to, doubles.size());
            for (double d : doubles) {
                to.writeDouble(d);
            }
        }
//        profiler.endStartSection("byte_arrays");
        if (!byteArrays.isEmpty()) {
            if (debug) log("\nByte Array dictionary size = " + byteArrays.size());
            writeVarInt(to, byteArrays.size());
            for (ByteArrayList ba : byteArrays) {
                to.writeShort(ba.size());
                for (byte b : ba) {
                    to.writeByte(b);
                }
            }
        }
//        profiler.endStartSection("int_arrays");
        if (!intArrays.isEmpty()) {
            if (debug) log("\nInt Array dictionary size = " + intArrays.size());
            writeVarInt(to, intArrays.size());
            for (IntArrayList ia : intArrays) {
                to.writeShort(ia.size());
                for (int i : ia) {
                    to.writeInt(i);
                }
            }
        }
//        profiler.endStartSection("strings");
        if (!strings.isEmpty()) {
            if (debug) log("\nString dictionary size = " + strings.size());
            if (sort) Collections.sort(strings);
            writeVarInt(to, strings.size());
            for (int i = 0; i < strings.size(); i++) {
                String s = strings.get(i);
                if (debug) log("\n   String " + i + " = " + s);
                byte[] stringBytes = s.getBytes(StandardCharsets.UTF_8);
                to.writeShort(stringBytes.length);
                to.write(stringBytes);
            }
        }
//        profiler.endStartSection("complex");
        if (!complex.isEmpty()) {
            if (debug) log("\nComplex dictionary size = " + complex.size());
            writeVarInt(to, complex.size());
            for (Tag nbt : complex) {
                if (nbt instanceof ListTag) {
                    ListTag list = (ListTag) nbt;
                    writeList(type, list, to);
                } else {
                    CompoundTag compound = (CompoundTag) nbt;
                    writeCompound(type, compound, to);
                }
            }
        }
//        profiler.endSection();
//        profiler.endSection();
    }

    /** Similar to {@link FrienflyByteBuf#writeVarInt(int)} */
    private static void writeVarInt(DataOutput to, int input) throws IOException {
        while ((input & -128) != 0) {
            to.writeByte((input & 0x7f) | 0x80);
            input >>>= 7;
        }
        to.writeByte(input);
    }

    private void writeList(WrittenType type, ListTag list, DataOutput to) throws IOException {
        boolean pack = shouldPackList(list);
        if (debug) log("\n  List tag count = " + list.size() + ", writing it " + (pack ? "PACKED" : "NORMAL"));
        if (pack) {
            writeListPacked(type, to, list);
        } else {
            writeListNormal(type, to, list);
        }
    }

    private boolean shouldPackList(ListTag list) {
        if (packList != null) return packList;
//        profiler.startSection("should_pack");
        IntOpenHashSet indexes = new IntOpenHashSet();
        for (int i = 0; i < list.size(); i++) {
            indexes.add(map.indexOfTag(list.get(i)));
        }
//        profiler.endSection();
        return indexes.size() * 2 < list.size();
    }

    private void writeCompound(WrittenType type, CompoundTag compound, DataOutput to) throws IOException {
//        profiler.startSection("compound");
        WrittenType stringType = WrittenType.getForSize(map.strings.size());
        if (debug) log("\n  Compound tag count = " + compound.size());
        to.writeByte(NbtSquishConstants.COMPLEX_COMPOUND);
        writeVarInt(to, compound.size());
        for (String key : compound.getAllKeys()) {
//            profiler.startSection("entry");
            Tag nbt = compound.get(key);
//            profiler.startSection("index_value");
            int index = map.indexOfTag(nbt);
//            profiler.endSection();
            if (debug) log("\n             \"" + key + "\" -> " + index + " (" + safeToString(nbt) + ")");
//            profiler.startSection("index_key");
            stringType.writeIndex(to, map.strings.indexOf(key));
//            profiler.endSection();
            type.writeIndex(to, index);
//            profiler.endSection();
        }
//        profiler.endSection();
    }

    private void writeListNormal(WrittenType type, DataOutput to, ListTag list) throws IOException {
//        profiler.startSection("list_normal");
        to.writeByte(NbtSquishConstants.COMPLEX_LIST);
        writeVarInt(to, list.size());
        for (int i = 0; i < list.size(); i++) {
//            profiler.startSection("entry");
            if (i % 100 == 0) {
                if (debug) log("\n   List items " + i + " to " + Math.min(i + 99, list.size()));
            }
//            profiler.startSection("index");
            int index = map.indexOfTag(list.get(i));
//            profiler.endSection();
            type.writeIndex(to, index);
//            profiler.endSection();
        }
//        profiler.endSection();
    }

    private void writeListPacked(WrittenType type, DataOutput to, ListTag list) throws IOException {
//        profiler.startSection("list_packed");
        to.writeByte(NbtSquishConstants.COMPLEX_LIST_PACKED);
//        profiler.startSection("header");
//        profiler.startSection("init");
        int[] data = new int[list.size()];
        Int2IntOpenHashMap indexes = new Int2IntOpenHashMap();
        indexes.defaultReturnValue(0xABCD);
        for (int i = 0; i < list.size(); i++) {
//            profiler.startSection("entry");
//            profiler.startSection("index");
            int index = map.indexOfTag(list.get(i));
//            profiler.endSection();
            data[i] = index;
            if (0xABCD == indexes.addTo(index, 1)) {
                indexes.put(index, 1);
            }
//            profiler.endSection();
        }
        // First try to make a simple table

        // First sort the indexes into highest count first
//        profiler.endStartSection("sort");
        List<IndexEntry> entries = new ArrayList<>();
        for (int index : indexes.keySet()) {
            int count = indexes.get(index);
            IndexEntry entry = new IndexEntry(index, count);
            entries.add(entry);
        }
        entries.sort(Comparator.reverseOrder());
        if (debug) log("\n " + entries.size() + " List entries");
        writeVarInt(to, entries.size());
//        profiler.endStartSection("write");

        IntArrayList sortedIndexes = new IntArrayList();
        int i = 0;
        for (IndexEntry entry : entries) {
            final int j = i;

            Tag base = map.getTagForWriting(entry.index);
            String n = safeToString(base);
            if (debug) log("\n List entry #" + j + " = " + entry.count + "x" + entry.index + " (" + n + ")");

            sortedIndexes.add(entry.index);
            type.writeIndex(to, entry.index);
            i++;
        }

        IntArrayList nextData = new IntArrayList();
        nextData.addAll(IntList.of(data));
        writeVarInt(to, data.length);
//        profiler.endSection();
//        profiler.endStartSection("contents");
        for (int b = 1; !nextData.isEmpty(); b++) {
//            profiler.startSection("entry");
            CompactingBitSet bitset = new CompactingBitSet(b);
            bitset.ensureCapacityValues(nextData.size());
            IntArrayList nextNextData = new IntArrayList();
            int maxVal = (1 << b) - 1;
//            profiler.startSection("iter");
            for (int d : nextData) {
                // profiler.startSection("entry");
                // profiler.startSection("index");
                int index = sortedIndexes.indexOf(d);
                // profiler.endSection();
                if (index < maxVal) {
                    // profiler.startSection("bitset_append");
                    bitset.append(index);
                    // profiler.endSection();
                } else {
                    // profiler.startSection("bitset_append");
                    bitset.append(maxVal);
                    // profiler.endStartSection("next_add");
                    nextNextData.add(d);
                    // profiler.endSection();
                }
                // profiler.endSection();
            }
//            profiler.endSection();
            sortedIndexes.removeElements(0, Math.min(sortedIndexes.size(), maxVal));
            byte[] bitsetBytes = bitset.getBytes();
            if (debug) log("\n List bitset #" + (bitset.bits - 1));
            writeVarInt(to, bitsetBytes.length);
            to.write(bitsetBytes);
            nextData = nextNextData;
//            profiler.endSection();
        }
//        profiler.endSection();
//        profiler.endSection();
    }

    public static String safeToString(Tag base) {
        String n = base.toString();
        if (n.length() > 100) {
            n = "[LARGE  " + n.substring(0, 100) + " ]";
        }
        return n;
    }

    private static class IndexEntry implements Comparable<IndexEntry> {
        public final int index, count;

        public IndexEntry(int index, int count) {
            this.index = index;
            this.count = count;
        }

        @Override
        public int compareTo(IndexEntry o) {
            return Integer.compare(count, o.count);
        }

        @Override
        public String toString() {
            return index + " x " + count;
        }
    }
}
