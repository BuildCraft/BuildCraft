package buildcraft.lib.nbt;

import static buildcraft.api.data.NBTSquishConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.profiler.Profiler;

import buildcraft.lib.misc.data.CompactingBitSet;

import gnu.trove.list.array.*;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

class NBTSquishMapWriter {
    static boolean debug;
    static final boolean sort = true;
    static final Boolean packList = null;
    static final Profiler profiler = NbtSquisher.profiler;
    private final NBTSquishMap map;

    private static void log(String string) {
        if (debug) {
            System.out.println(string);
        } else {
            throw new IllegalArgumentException("Don't allocate a string if we aren't debugging!");
        }
    }

    public NBTSquishMapWriter(NBTSquishMap map) {
        this.map = map;
    }

    public static void write(NBTSquishMap map, PacketBuffer buf) {
        new NBTSquishMapWriter(map).write(buf);
    }

    private void write(PacketBuffer buf) {
        profiler.startSection("write");
        profiler.startSection("flags");
        WrittenType type = map.getWrittenType();

        type.writeType(buf);

        TByteArrayList bytes = map.bytes;
        TShortArrayList shorts = map.shorts;
        TIntArrayList ints = map.ints;
        TLongArrayList longs = map.longs;
        TFloatArrayList floats = map.floats;
        TDoubleArrayList doubles = map.doubles;
        List<TByteArrayList> byteArrays = map.byteArrays;
        List<TIntArrayList> intArrays = map.intArrays;
        List<String> strings = map.strings;
        List<NBTBase> complex = map.complex;

        int flags = 0;
        if (!bytes.isEmpty()) flags |= FLAG_HAS_BYTES;
        if (!shorts.isEmpty()) flags |= FLAG_HAS_SHORTS;
        if (!ints.isEmpty()) flags |= FLAG_HAS_INTS;
        if (!longs.isEmpty()) flags |= FLAG_HAS_LONGS;
        if (!floats.isEmpty()) flags |= FLAG_HAS_FLOATS;
        if (!doubles.isEmpty()) flags |= FLAG_HAS_DOUBLES;
        if (!byteArrays.isEmpty()) flags |= FLAG_HAS_BYTE_ARRAYS;
        if (!intArrays.isEmpty()) flags |= FLAG_HAS_INT_ARRAYS;
        if (!strings.isEmpty()) flags |= FLAG_HAS_STRINGS;
        if (!complex.isEmpty()) flags |= FLAG_HAS_COMPLEX;

        if (debug) log("\nUsed flags = " + Integer.toBinaryString(flags));
        buf.writeInt(flags);

        profiler.endStartSection("bytes");
        if (!bytes.isEmpty()) {
            if (debug) log("\nByte dictionary size = " + bytes.size());
            if (sort) bytes.sort();
            buf.writeVarInt(bytes.size());
            for (byte b : bytes.toArray()) {
                buf.writeByte(b);
            }
        }
        profiler.endStartSection("shorts");
        if (!shorts.isEmpty()) {
            if (debug) log("\nShort dictionary size = " + shorts.size());
            if (sort) shorts.sort();
            buf.writeVarInt(shorts.size());
            for (short s : shorts.toArray()) {
                buf.writeShort(s);
            }
        }
        profiler.endStartSection("integers");
        if (!ints.isEmpty()) {
            if (debug) log("\nInt dictionary size = " + ints.size());
            if (sort) ints.sort();
            buf.writeVarInt(ints.size());
            for (int i : ints.toArray()) {
                buf.writeInt(i);
            }
        }
        profiler.endStartSection("longs");
        if (!longs.isEmpty()) {
            if (debug) log("\nLong dictionary size = " + longs.size());
            if (sort) longs.sort();
            buf.writeVarInt(longs.size());
            for (long l : longs.toArray()) {
                buf.writeLong(l);
            }
        }
        profiler.endStartSection("floats");
        if (!floats.isEmpty()) {
            if (debug) log("\nFloat dictionary size = " + floats.size());
            if (sort) floats.sort();
            buf.writeVarInt(floats.size());
            for (float f : floats.toArray()) {
                buf.writeFloat(f);
            }
        }
        profiler.endStartSection("doubles");
        if (!doubles.isEmpty()) {
            if (debug) log("\nDouble dictionary size = " + doubles.size());
            if (sort) doubles.sort();
            buf.writeVarInt(doubles.size());
            for (double d : doubles.toArray()) {
                buf.writeDouble(d);
            }
        }
        profiler.endStartSection("byte_arrays");
        if (!byteArrays.isEmpty()) {
            if (debug) log("\nByte Array dictionary size = " + byteArrays.size());
            buf.writeVarInt(byteArrays.size());
            for (TByteArrayList ba : byteArrays) {
                buf.writeShort(ba.size());
                for (byte b : ba.toArray()) {
                    buf.writeByte(b);
                }
            }
        }
        profiler.endStartSection("int_arrays");
        if (!intArrays.isEmpty()) {
            if (debug) log("\nInt Array dictionary size = " + intArrays.size());
            buf.writeVarInt(intArrays.size());
            for (TIntArrayList ia : intArrays) {
                buf.writeShort(ia.size());
                for (int i : ia.toArray()) {
                    buf.writeInt(i);
                }
            }
        }
        profiler.endStartSection("strings");
        if (!strings.isEmpty()) {
            if (debug) log("\nString dictionary size = " + strings.size());
            if (sort) Collections.sort(strings);
            buf.writeVarInt(strings.size());
            for (int i = 0; i < strings.size(); i++) {
                String s = strings.get(i);
                if (debug) log("\n   String " + i + " = " + s);
                byte[] stringBytes = s.getBytes(StandardCharsets.UTF_8);
                buf.writeShort(stringBytes.length);
                buf.writeBytes(stringBytes);
            }
        }
        profiler.endStartSection("complex");
        if (!complex.isEmpty()) {
            if (debug) log("\nComplex dictionary size = " + complex.size());
            buf.writeVarInt(complex.size());
            for (NBTBase nbt : complex) {
                if (nbt instanceof NBTTagList) {
                    NBTTagList list = (NBTTagList) nbt;
                    writeList(type, list, buf);
                } else {
                    NBTTagCompound compound = (NBTTagCompound) nbt;
                    writeCompound(type, compound, buf);
                }
            }
        }
        profiler.endSection();
        profiler.endSection();
    }

    private void writeList(WrittenType type, NBTTagList list, PacketBuffer buf) {
        boolean pack = shouldPackList(list);
        if (debug) log("\n  List tag count = " + list.tagCount() + ", writing it " + (pack ? "PACKED" : "NORMAL"));
        if (pack) {
            writeListPacked(type, buf, list);
        } else {
            writeListNormal(type, buf, list);
        }
    }

    private boolean shouldPackList(NBTTagList list) {
        if (packList != null) return packList.booleanValue();
        profiler.startSection("should_pack");
        TIntHashSet indexes = new TIntHashSet();
        for (int i = 0; i < list.tagCount(); i++) {
            indexes.add(map.indexOfTag(list.get(i)));
        }
        profiler.endSection();
        return indexes.size() * 2 < list.tagCount();
    }

    private void writeCompound(WrittenType type, NBTTagCompound compound, PacketBuffer buf) {
        profiler.startSection("compound");
        WrittenType stringType = WrittenType.getForSize(map.strings.size());
        if (debug) log("\n  Compound tag count = " + compound.getSize());
        buf.writeByte(COMPLEX_COMPOUND);
        buf.writeVarInt(compound.getSize());
        for (String key : compound.getKeySet()) {
            profiler.startSection("entry");
            NBTBase nbt = compound.getTag(key);
            profiler.startSection("index_value");
            int index = map.indexOfTag(nbt);
            profiler.endSection();
            if (debug) log("\n             \"" + key + "\" -> " + index + " (" + safeToString(nbt) + ")");
            profiler.startSection("index_key");
            stringType.writeIndex(buf, map.strings.indexOf(key));
            profiler.endSection();
            type.writeIndex(buf, index);
            profiler.endSection();
        }
        profiler.endSection();
    }

    private void writeListNormal(WrittenType type, PacketBuffer buf, NBTTagList list) {
        profiler.startSection("list_normal");
        buf.writeByte(COMPLEX_LIST);
        buf.writeVarInt(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            profiler.startSection("entry");
            if (i % 100 == 0) {
                if (debug) log("\n   List items " + i + " to " + Math.min(i + 99, list.tagCount()));
            }
            profiler.startSection("index");
            int index = map.indexOfTag(list.get(i));
            profiler.endSection();
            type.writeIndex(buf, index);
            profiler.endSection();
        }
        profiler.endSection();
    }

    private void writeListPacked(WrittenType type, PacketBuffer buf, NBTTagList list) {
        profiler.startSection("list_packed");
        buf.writeByte(COMPLEX_LIST_PACKED);
        profiler.startSection("header");
        profiler.startSection("init");
        int[] data = new int[list.tagCount()];
        TIntIntHashMap indexes = new TIntIntHashMap();
        for (int i = 0; i < list.tagCount(); i++) {
            profiler.startSection("entry");
            profiler.startSection("index");
            int index = map.indexOfTag(list.get(i));
            profiler.endSection();
            data[i] = index;
            if (!indexes.increment(index)) {
                indexes.put(index, 1);
            }
            profiler.endSection();
        }
        // First try to make a simple table

        // First sort the indexes into highest count first
        profiler.endStartSection("sort");
        List<IndexEntry> entries = new ArrayList<>();
        for (int index : indexes.keys()) {
            int count = indexes.get(index);
            IndexEntry entry = new IndexEntry(index, count);
            entries.add(entry);
        }
        entries.sort(Comparator.reverseOrder());
        if (debug) log("\n " + entries.size() + " List entries");
        buf.writeVarInt(entries.size());
        profiler.endStartSection("write");

        TIntArrayList sortedIndexes = new TIntArrayList();
        int i = 0;
        for (IndexEntry entry : entries) {
            final int j = i;

            NBTBase base = map.getTagForWriting(entry.index);
            String n = safeToString(base);
            if (debug) log("\n List entry #" + j + " = " + entry.count + "x" + entry.index + " (" + n + ")");

            sortedIndexes.add(entry.index);
            type.writeIndex(buf, entry.index);
            i++;
        }

        TIntArrayList nextData = new TIntArrayList();
        nextData.add(data);
        buf.writeVarInt(data.length);
        profiler.endSection();
        profiler.endStartSection("contents");
        for (int b = 1; !nextData.isEmpty(); b++) {
            profiler.startSection("entry");
            CompactingBitSet bitset = new CompactingBitSet(b);
            bitset.ensureCapacityValues(nextData.size());
            TIntArrayList nextNextData = new TIntArrayList();
            int maxVal = (1 << b) - 1;
            profiler.startSection("iter");
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
            profiler.endSection();
            sortedIndexes.remove(0, Math.min(sortedIndexes.size(), maxVal));
            byte[] bitsetBytes = bitset.getBytes();
            if (debug) log("\n List bitset #" + (bitset.bits - 1));
            buf.writeVarInt(bitsetBytes.length);
            buf.writeBytes(bitsetBytes);
            nextData = nextNextData;
            profiler.endSection();
        }
        profiler.endSection();
        profiler.endSection();
    }

    public static String safeToString(NBTBase base) {
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
