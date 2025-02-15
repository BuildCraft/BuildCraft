/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import buildcraft.api.data.NbtSquishConstants;
import buildcraft.lib.misc.data.CompactingBitSet;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteComparators;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatComparators;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongComparators;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortComparators;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.profiling.ProfilerFiller;

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
    static final ProfilerFiller profiler = NbtSquisher.profiler;
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

        profiler.popPush("bytes");
        if (!bytes.isEmpty()) {
            if (debug) log("\nByte dictionary size = " + bytes.size());
            if (sort) bytes.sort(ByteComparators.NATURAL_COMPARATOR);
            writeVarInt(to, bytes.size());
            for (byte b : bytes.toByteArray()) {
                to.writeByte(b);
            }
        }
        profiler.popPush("shorts");
        if (!shorts.isEmpty()) {
            if (debug) log("\nShort dictionary size = " + shorts.size());
            if (sort) shorts.sort(ShortComparators.NATURAL_COMPARATOR);
            writeVarInt(to, shorts.size());
            for (short s : shorts.toShortArray()) {
                to.writeShort(s);
            }
        }
        profiler.popPush("integers");
        if (!ints.isEmpty()) {
            if (debug) log("\nInt dictionary size = " + ints.size());
            if (sort) ints.sort(IntComparators.NATURAL_COMPARATOR);
            writeVarInt(to, ints.size());
            for (int i : ints.toIntArray()) {
                to.writeInt(i);
            }
        }
        profiler.popPush("longs");
        if (!longs.isEmpty()) {
            if (debug) log("\nLong dictionary size = " + longs.size());
            if (sort) longs.sort(LongComparators.NATURAL_COMPARATOR);
            writeVarInt(to, longs.size());
            for (long l : longs.toLongArray()) {
                to.writeLong(l);
            }
        }
        profiler.popPush("floats");
        if (!floats.isEmpty()) {
            if (debug) log("\nFloat dictionary size = " + floats.size());
            if (sort) floats.sort(FloatComparators.NATURAL_COMPARATOR);
            writeVarInt(to, floats.size());
            for (float f : floats.toFloatArray()) {
                to.writeFloat(f);
            }
        }
        profiler.popPush("doubles");
        if (!doubles.isEmpty()) {
            if (debug) log("\nDouble dictionary size = " + doubles.size());
            if (sort) doubles.sort(DoubleComparators.NATURAL_COMPARATOR);
            writeVarInt(to, doubles.size());
            for (double d : doubles.toDoubleArray()) {
                to.writeDouble(d);
            }
        }
        profiler.popPush("byte_arrays");
        if (!byteArrays.isEmpty()) {
            if (debug) log("\nByte Array dictionary size = " + byteArrays.size());
            writeVarInt(to, byteArrays.size());
            for (ByteArrayList ba : byteArrays) {
                to.writeShort(ba.size());
                for (byte b : ba.toByteArray()) {
                    to.writeByte(b);
                }
            }
        }
        profiler.popPush("int_arrays");
        if (!intArrays.isEmpty()) {
            if (debug) log("\nInt Array dictionary size = " + intArrays.size());
            writeVarInt(to, intArrays.size());
            for (IntArrayList ia : intArrays) {
                to.writeShort(ia.size());
                for (int i : ia.toIntArray()) {
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
        profiler.pop();
        profiler.pop();
    }

    /** Similar to {@link FriendlyByteBuf#writeVarInt(int)} */
    private static void writeVarInt(DataOutput to, int input) throws IOException {
        while ((input & -128) != 0) {
            to.writeByte((input & 0x7f) | 0x80);
            input >>>= 7;
        }
        to.writeByte(input);
    }

    private void writeList(WrittenType type, ListTag list, DataOutput to) throws IOException {
        boolean pack = shouldPackList(list);
//        if (debug) log("\n  List tag count = " + list.tagCount() + ", writing it " + (pack ? "PACKED" : "NORMAL"));
        if (debug) log("\n  List tag count = " + list.size() + ", writing it " + (pack ? "PACKED" : "NORMAL"));
        if (pack) {
            writeListPacked(type, to, list);
        } else {
            writeListNormal(type, to, list);
        }
    }

    private boolean shouldPackList(ListTag list) {
        if (packList != null) return packList;
        profiler.push("should_pack");
        IntOpenHashSet indexes = new IntOpenHashSet();
//        for (int i = 0; i < list.tagCount(); i++)
        for (int i = 0; i < list.size(); i++) {
            indexes.add(map.indexOfTag(list.get(i)));
        }
        profiler.pop();
//        return indexes.size() * 2 < list.tagCount();
        return indexes.size() * 2 < list.size();
    }

    private void writeCompound(WrittenType type, CompoundTag compound, DataOutput to) throws IOException {
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
            Tag nbt = compound.get(key);
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

    private void writeListNormal(WrittenType type, DataOutput to, ListTag list) throws IOException {
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

    private void writeListPacked(WrittenType type, DataOutput to, ListTag list) throws IOException {
        profiler.push("list_packed");
        to.writeByte(NbtSquishConstants.COMPLEX_LIST_PACKED);
        profiler.push("header");
        profiler.push("init");
//        int[] data = new int[list.tagCount()];
        int[] data = new int[list.size()];
        Int2IntOpenHashMap indexes = new Int2IntOpenHashMap();
//        for (int i = 0; i < list.tagCount(); i++)
        for (int i = 0; i < list.size(); i++) {
            profiler.push("entry");
            profiler.push("index");
            int index = map.indexOfTag(list.get(i));
            profiler.pop();
            data[i] = index;
//            if (!indexes.increment(index)) {
//                indexes.put(index, 1);
//            }
            indexes.addTo(index, 1);
            profiler.pop();
        }
        // First try to make a simple table

        // First sort the indexes into highest count first
        profiler.popPush("sort");
        List<IndexEntry> entries = new ArrayList<>();
        for (int index : indexes.keySet()) {
            int count = indexes.get(index);
            IndexEntry entry = new IndexEntry(index, count);
            entries.add(entry);
        }
        entries.sort(Comparator.reverseOrder());
        if (debug) log("\n " + entries.size() + " List entries");
        writeVarInt(to, entries.size());
        profiler.popPush("write");

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
        nextData.addElements(nextData.size(), data);
        writeVarInt(to, data.length);
        profiler.pop();
        profiler.popPush("contents");
        for (int b = 1; !nextData.isEmpty(); b++) {
            profiler.push("entry");
            CompactingBitSet bitset = new CompactingBitSet(b);
            bitset.ensureCapacityValues(nextData.size());
            IntArrayList nextNextData = new IntArrayList();
            int maxVal = (1 << b) - 1;
            profiler.push("iter");
            for (int d : nextData.toIntArray()) {
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
            sortedIndexes.removeElements(0, 0 + Math.min(sortedIndexes.size(), maxVal));
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
