/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import buildcraft.api.data.NbtSquishConstants;
import buildcraft.lib.misc.data.CompactingBitSet;
import gnu.trove.list.array.*;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.registry.Bootstrap;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class NbtSquishMapWriter {
    static boolean debug;
    static final boolean sort = true;
    static final Boolean packList = null;
    // static final Profiler profiler = NbtSquisher.profiler;
    static final Profiler profiler = NbtSquisher.profiler;
    private final NbtSquishMap map;

    private static void log(String string) {
        if (debug) {
//            Bootstrap.SYSOUT.print(string + "\n");
            Bootstrap.STDOUT.print(string + "\n");
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
        profiler.push("write");
        profiler.push("flags");
        WrittenType type = map.getWrittenType();

        type.writeType(to);

        TByteArrayList bytes = map.bytes;
        TShortArrayList shorts = map.shorts;
        TIntArrayList ints = map.ints;
        TLongArrayList longs = map.longs;
        TFloatArrayList floats = map.floats;
        TDoubleArrayList doubles = map.doubles;
        List<TByteArrayList> byteArrays = map.byteArrays;
        List<TIntArrayList> intArrays = map.intArrays;
        List<String> strings = map.strings;
        List<INBT> complex = map.complex;

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

        profiler.popPush("bytes");
        if (!bytes.isEmpty()) {
            if (debug) log("\nByte dictionary size = " + bytes.size());
            if (sort) bytes.sort();
            writeVarInt(to, bytes.size());
            for (byte b : bytes.toArray()) {
                to.writeByte(b);
            }
        }
        profiler.popPush("shorts");
        if (!shorts.isEmpty()) {
            if (debug) log("\nShort dictionary size = " + shorts.size());
            if (sort) shorts.sort();
            writeVarInt(to, shorts.size());
            for (short s : shorts.toArray()) {
                to.writeShort(s);
            }
        }
        profiler.popPush("integers");
        if (!ints.isEmpty()) {
            if (debug) log("\nInt dictionary size = " + ints.size());
            if (sort) ints.sort();
            writeVarInt(to, ints.size());
            for (int i : ints.toArray()) {
                to.writeInt(i);
            }
        }
        profiler.popPush("longs");
        if (!longs.isEmpty()) {
            if (debug) log("\nLong dictionary size = " + longs.size());
            if (sort) longs.sort();
            writeVarInt(to, longs.size());
            for (long l : longs.toArray()) {
                to.writeLong(l);
            }
        }
        profiler.popPush("floats");
        if (!floats.isEmpty()) {
            if (debug) log("\nFloat dictionary size = " + floats.size());
            if (sort) floats.sort();
            writeVarInt(to, floats.size());
            for (float f : floats.toArray()) {
                to.writeFloat(f);
            }
        }
        profiler.popPush("doubles");
        if (!doubles.isEmpty()) {
            if (debug) log("\nDouble dictionary size = " + doubles.size());
            if (sort) doubles.sort();
            writeVarInt(to, doubles.size());
            for (double d : doubles.toArray()) {
                to.writeDouble(d);
            }
        }
        profiler.popPush("byte_arrays");
        if (!byteArrays.isEmpty()) {
            if (debug) log("\nByte Array dictionary size = " + byteArrays.size());
            writeVarInt(to, byteArrays.size());
            for (TByteArrayList ba : byteArrays) {
                to.writeShort(ba.size());
                for (byte b : ba.toArray()) {
                    to.writeByte(b);
                }
            }
        }
        profiler.popPush("int_arrays");
        if (!intArrays.isEmpty()) {
            if (debug) log("\nInt Array dictionary size = " + intArrays.size());
            writeVarInt(to, intArrays.size());
            for (TIntArrayList ia : intArrays) {
                to.writeShort(ia.size());
                for (int i : ia.toArray()) {
                    to.writeInt(i);
                }
            }
        }
        profiler.popPush("strings");
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
        profiler.popPush("complex");
        if (!complex.isEmpty()) {
            if (debug) log("\nComplex dictionary size = " + complex.size());
            writeVarInt(to, complex.size());
            for (INBT nbt : complex) {
                if (nbt instanceof ListNBT) {
                    ListNBT list = (ListNBT) nbt;
                    writeList(type, list, to);
                } else {
                    CompoundNBT compound = (CompoundNBT) nbt;
                    writeCompound(type, compound, to);
                }
            }
        }
        profiler.pop();
        profiler.pop();
    }

    /** Similar to {@link PacketBuffer#writeVarInt(int)} */
    private static void writeVarInt(DataOutput to, int input) throws IOException {
        while ((input & -128) != 0) {
            to.writeByte((input & 0x7f) | 0x80);
            input >>>= 7;
        }
        to.writeByte(input);
    }

    private void writeList(WrittenType type, ListNBT list, DataOutput to) throws IOException {
        boolean pack = shouldPackList(list);
//        if (debug) log("\n  List tag count = " + list.tagCount() + ", writing it " + (pack ? "PACKED" : "NORMAL"));
        if (debug) log("\n  List tag count = " + list.size() + ", writing it " + (pack ? "PACKED" : "NORMAL"));
        if (pack) {
            writeListPacked(type, to, list);
        } else {
            writeListNormal(type, to, list);
        }
    }

    private boolean shouldPackList(ListNBT list) {
        if (packList != null) return packList;
        profiler.push("should_pack");
        TIntHashSet indexes = new TIntHashSet();
//        for (int i = 0; i < list.tagCount(); i++)
        for (int i = 0; i < list.size(); i++) {
            indexes.add(map.indexOfTag(list.get(i)));
        }
        profiler.pop();
//        return indexes.size() * 2 < list.tagCount();
        return indexes.size() * 2 < list.size();
    }

    private void writeCompound(WrittenType type, CompoundNBT compound, DataOutput to) throws IOException {
        profiler.push("compound");
        WrittenType stringType = WrittenType.getForSize(map.strings.size());
//        if (debug) log("\n  Compound tag count = " + compound.getSize());
        if (debug) log("\n  Compound tag count = " + compound.size());
        to.writeByte(NbtSquishConstants.COMPLEX_COMPOUND);
//        writeVarInt(to, compound.getSize());
        writeVarInt(to, compound.size());
//        for (String key : compound.getKeySet())
        for (String key : compound.getAllKeys()) {
            profiler.push("entry");
            INBT nbt = compound.get(key);
            profiler.push("index_value");
            int index = map.indexOfTag(nbt);
            profiler.pop();
            if (debug) log("\n             \"" + key + "\" -> " + index + " (" + safeToString(nbt) + ")");
            profiler.push("index_key");
            stringType.writeIndex(to, map.strings.indexOf(key));
            profiler.pop();
            type.writeIndex(to, index);
            profiler.pop();
        }
        profiler.pop();
    }

    private void writeListNormal(WrittenType type, DataOutput to, ListNBT list) throws IOException {
        profiler.push("list_normal");
        to.writeByte(NbtSquishConstants.COMPLEX_LIST);
//        writeVarInt(to, list.tagCount());
        writeVarInt(to, list.size());
//        for (int i = 0; i < list.tagCount(); i++)
        for (int i = 0; i < list.size(); i++) {
            profiler.push("entry");
            if (i % 100 == 0) {
//                if (debug) log("\n   List items " + i + " to " + Math.min(i + 99, list.tagCount()));
                if (debug) log("\n   List items " + i + " to " + Math.min(i + 99, list.size()));
            }
            profiler.push("index");
            int index = map.indexOfTag(list.get(i));
            profiler.pop();
            type.writeIndex(to, index);
            profiler.pop();
        }
        profiler.pop();
    }

    private void writeListPacked(WrittenType type, DataOutput to, ListNBT list) throws IOException {
        profiler.push("list_packed");
        to.writeByte(NbtSquishConstants.COMPLEX_LIST_PACKED);
        profiler.push("header");
        profiler.push("init");
//        int[] data = new int[list.tagCount()];
        int[] data = new int[list.size()];
        TIntIntHashMap indexes = new TIntIntHashMap();
//        for (int i = 0; i < list.tagCount(); i++)
        for (int i = 0; i < list.size(); i++) {
            profiler.push("entry");
            profiler.push("index");
            int index = map.indexOfTag(list.get(i));
            profiler.pop();
            data[i] = index;
            if (!indexes.increment(index)) {
                indexes.put(index, 1);
            }
            profiler.pop();
        }
        // First try to make a simple table

        // First sort the indexes into highest count first
        profiler.popPush("sort");
        List<IndexEntry> entries = new ArrayList<>();
        for (int index : indexes.keys()) {
            int count = indexes.get(index);
            IndexEntry entry = new IndexEntry(index, count);
            entries.add(entry);
        }
        entries.sort(Comparator.reverseOrder());
        if (debug) log("\n " + entries.size() + " List entries");
        writeVarInt(to, entries.size());
        profiler.popPush("write");

        TIntArrayList sortedIndexes = new TIntArrayList();
        int i = 0;
        for (IndexEntry entry : entries) {
            final int j = i;

            INBT base = map.getTagForWriting(entry.index);
            String n = safeToString(base);
            if (debug) log("\n List entry #" + j + " = " + entry.count + "x" + entry.index + " (" + n + ")");

            sortedIndexes.add(entry.index);
            type.writeIndex(to, entry.index);
            i++;
        }

        TIntArrayList nextData = new TIntArrayList();
        nextData.add(data);
        writeVarInt(to, data.length);
        profiler.pop();
        profiler.popPush("contents");
        for (int b = 1; !nextData.isEmpty(); b++) {
            profiler.push("entry");
            CompactingBitSet bitset = new CompactingBitSet(b);
            bitset.ensureCapacityValues(nextData.size());
            TIntArrayList nextNextData = new TIntArrayList();
            int maxVal = (1 << b) - 1;
            profiler.push("iter");
            for (int d : nextData.toArray()) {
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
            profiler.pop();
            sortedIndexes.remove(0, Math.min(sortedIndexes.size(), maxVal));
            byte[] bitsetBytes = bitset.getBytes();
            if (debug) log("\n List bitset #" + (bitset.bits - 1));
            writeVarInt(to, bitsetBytes.length);
            to.write(bitsetBytes);
            nextData = nextNextData;
            profiler.pop();
        }
        profiler.pop();
        profiler.pop();
    }

    public static String safeToString(INBT base) {
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
