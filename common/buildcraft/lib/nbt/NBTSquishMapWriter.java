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

import buildcraft.lib.misc.data.CompactingBitSet;

import gnu.trove.list.array.*;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import io.netty.buffer.ByteBuf;

class NBTSquishMapWriter {
    private final NBTSquishMap map;

    public NBTSquishMapWriter(NBTSquishMap map) {
        this.map = map;
    }

    public static void write(NBTSquishMap map, ByteBuf buf) {
        new NBTSquishMapWriter(map).write(buf);
    }

    private void write(ByteBuf buf) {
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

        final int flags2 = flags;
        NBTSquishDebugging.log(() -> "\nUsed flags = " + Integer.toBinaryString(flags2));
        buf.writeInt(flags);

        if (!bytes.isEmpty()) {
            bytes.sort();
            NBTSquishDebugging.log(() -> "\nByte dictionary size = " + bytes.size());
            buf.writeShort(bytes.size());
            for (byte b : bytes.toArray()) {
                buf.writeByte(b);
            }
        }

        if (!shorts.isEmpty()) {
            shorts.sort();
            NBTSquishDebugging.log(() -> "\nShort dictionary size = " + shorts.size());
            buf.writeShort(shorts.size());
            for (short s : shorts.toArray()) {
                buf.writeShort(s);
            }
        }
        if (!ints.isEmpty()) {
            ints.sort();
            NBTSquishDebugging.log(() -> "\nInt dictionary size = " + ints.size());
            buf.writeShort(ints.size());
            for (int i : ints.toArray()) {
                buf.writeInt(i);
            }
        }

        if (!longs.isEmpty()) {
            longs.sort();
            NBTSquishDebugging.log(() -> "\nLong dictionary size = " + longs.size());
            buf.writeShort(longs.size());
            for (long l : longs.toArray()) {
                buf.writeLong(l);
            }
        }

        if (!floats.isEmpty()) {
            floats.sort();
            NBTSquishDebugging.log(() -> "\nFloat dictionary size = " + floats.size());
            buf.writeShort(floats.size());
            for (float f : floats.toArray()) {
                buf.writeFloat(f);
            }
        }
        if (!doubles.isEmpty()) {
            doubles.sort();
            NBTSquishDebugging.log(() -> "\nDouble dictionary size = " + doubles.size());
            buf.writeShort(doubles.size());
            for (double d : doubles.toArray()) {
                buf.writeDouble(d);
            }
        }

        if (!byteArrays.isEmpty()) {
            // We don't sort arrays because they are more expensive. (and its not a one liner :P)
            NBTSquishDebugging.log(() -> "\nByte Array dictionary size = " + byteArrays.size());
            buf.writeShort(byteArrays.size());
            for (TByteArrayList ba : byteArrays) {
                buf.writeShort(ba.size());
                for (byte b : ba.toArray()) {
                    buf.writeByte(b);
                }
            }
        }

        if (!intArrays.isEmpty()) {
            NBTSquishDebugging.log(() -> "\nInt Array dictionary size = " + intArrays.size());
            buf.writeShort(intArrays.size());
            for (TIntArrayList ia : intArrays) {
                buf.writeShort(ia.size());
                for (int i : ia.toArray()) {
                    buf.writeInt(i);
                }
            }
        }

        if (!strings.isEmpty()) {
            // Sort strings beforehand. I don't know if this makes a difference or not, but it might help gzip to
            // compress
            // Similar strings more (as they are closer).
            Collections.sort(strings);
            NBTSquishDebugging.log(() -> "\nString dictionary size = " + strings.size());
            buf.writeShort(strings.size());
            for (String s : strings) {
                byte[] stringBytes = s.getBytes(StandardCharsets.UTF_8);
                NBTSquishDebugging.log(() -> "\n  " + stringBytes.length + " bytes for \"" + s + "\"");
                buf.writeShort(stringBytes.length);
                buf.writeBytes(stringBytes);
            }
        }

        if (!complex.isEmpty()) {
            NBTSquishDebugging.log(() -> "\nComplex dictionary size = " + complex.size());
            buf.writeShort(complex.size());
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
    }

    private void writeList(WrittenType type, NBTTagList list, ByteBuf buf) {
        boolean pack = shouldPackList(list);
        NBTSquishDebugging.log(() -> "\n  List tag count = " + list.tagCount() + ", writing it " + (pack ? "PACKED" : "NORMAL"));
        if (pack) {
            writeListPacked(type, buf, list);
        } else {
            writeListNormal(type, buf, list);
        }
    }

    private boolean shouldPackList(NBTTagList list) {
        TIntHashSet indexes = new TIntHashSet();
        for (int i = 0; i < list.tagCount(); i++) {
            indexes.add(map.indexOfTag(list.get(i)));
        }
        return indexes.size() * 2 < list.tagCount();
    }

    private void writeCompound(WrittenType type, NBTTagCompound compound, ByteBuf buf) {
        WrittenType stringType = WrittenType.getForSize(map.strings.size());
        NBTSquishDebugging.log(() -> "\n  Compound tag count = " + compound.getSize());
        buf.writeByte(COMPLEX_COMPOUND);
        buf.writeShort(compound.getSize());
        for (String key : compound.getKeySet()) {
            NBTBase nbt = compound.getTag(key);
            int index = map.indexOfTag(nbt);
            NBTSquishDebugging.log(() -> "\n             \"" + key + "\" -> " + index + " (" + safeToString(nbt) + ")");
            stringType.writeIndex(buf, map.strings.indexOf(key));
            type.writeIndex(buf, index);
        }
    }

    private void writeListNormal(WrittenType type, ByteBuf buf, NBTTagList list) {
        buf.writeByte(COMPLEX_LIST);
        buf.writeShort(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            type.writeIndex(buf, map.indexOfTag(list.get(i)));
        }
    }

    private void writeListPacked(WrittenType type, ByteBuf buf, NBTTagList list) {
        buf.writeByte(COMPLEX_LIST_PACKED);
        int[] data = new int[list.tagCount()];
        TIntIntHashMap indexes = new TIntIntHashMap();
        for (int i = 0; i < list.tagCount(); i++) {
            int index = map.indexOfTag(list.get(i));
            data[i] = index;
            if (!indexes.increment(index)) {
                indexes.put(index, 1);
            }
        }
        // First try to make a simple table

        // First sort the indexes into highest count first
        List<IndexEntry> entries = new ArrayList<>();
        for (int index : indexes.keys()) {
            int count = indexes.get(index);
            IndexEntry entry = new IndexEntry(index, count);
            entries.add(entry);
        }
        entries.sort(Comparator.reverseOrder());
        NBTSquishDebugging.log(() -> "\n    " + entries.size() + " List entries");
        buf.writeShort(entries.size());

        TIntArrayList sortedIndexes = new TIntArrayList();
        int i = 0;
        for (IndexEntry entry : entries) {
            final int j = i;
            NBTSquishDebugging.log(() -> {
                NBTBase base = map.getTagForWriting(entry.index);
                String n = safeToString(base);
                return "\n      List entry #" + j + " = " + entry.count + "x" + entry.index + " (" + n + ")";
            });
            sortedIndexes.add(entry.index);
            type.writeIndex(buf, entry.index);
            i++;
        }

        TIntArrayList nextData = new TIntArrayList();
        nextData.add(data);
        buf.writeMedium(data.length);
        for (int b = 1; !nextData.isEmpty(); b++) {
            CompactingBitSet bitset = new CompactingBitSet(b);
            TIntArrayList nextNextData = new TIntArrayList();
            int maxVal = (1 << b) - 1;
            for (int d : nextData.toArray()) {
                int index = sortedIndexes.indexOf(d);
                if (index < maxVal) {
                    bitset.append(index);
                } else {
                    bitset.append(maxVal);
                    nextNextData.add(d);
                }
            }
            sortedIndexes.remove(0, Math.min(sortedIndexes.size(), maxVal));
            byte[] bitsetBytes = bitset.getBytes();
            NBTSquishDebugging.log(() -> "\n      List bitset #" + (bitset.bits - 1));
            buf.writeShort(bitsetBytes.length);
            buf.writeBytes(bitsetBytes);
            nextData = nextNextData;
        }
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
