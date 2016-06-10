package buildcraft.lib.nbt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.nbt.*;

import buildcraft.lib.misc.data.CompactingBitSet;

import gnu.trove.list.array.*;
import gnu.trove.map.hash.TIntIntHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** Defines a map of commonly used tags. */
public class NBTSquishMap {
    private static final int LIST_OPTIMIZE_MIN = 20;

    // I'm not completely convinced that this one is necessary.
    // However it completes the set so, meh
    private final TByteArrayList bytes = new TByteArrayList();

    private final TShortArrayList shorts = new TShortArrayList();
    private final TIntArrayList ints = new TIntArrayList();
    private final TLongArrayList longs = new TLongArrayList();
    private final TFloatArrayList floats = new TFloatArrayList();
    private final TDoubleArrayList doubles = new TDoubleArrayList();

    private final List<TByteArrayList> byteArrays = new ArrayList<>();
    private final List<TIntArrayList> intArrays = new ArrayList<>();

    private final List<String> strings = new ArrayList<>();
    private final List<NBTTagList> lists = new ArrayList<>();
    private final List<NBTTagCompound> compounds = new ArrayList<>();

    public NBTSquishMap() {}

    public NBTSquishMap(ByteBuf buf) throws IOException {
        WrittenType type = WrittenType.readType(buf);

        int flags = buf.readInt();

        if ((flags & 1) == 1) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                bytes.add(buf.readByte());
            }
        }

        if ((flags & 2) == 2) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                shorts.add(buf.readShort());
            }
        }

        if ((flags & 4) == 4) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                ints.add(buf.readInt());
            }
        }

        if ((flags & 8) == 8) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                longs.add(buf.readLong());
            }
        }

        if ((flags & 16) == 16) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                floats.add(buf.readFloat());
            }
        }

        if ((flags & 32) == 32) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                doubles.add(buf.readDouble());
            }
        }

        if ((flags & 64) == 64) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                int arraySize = buf.readUnsignedShort();
                TByteArrayList list = new TByteArrayList();
                for (int j = 0; j < arraySize; j++) {
                    list.add(buf.readByte());
                }
                byteArrays.add(list);
            }
        }

        if ((flags & 128) == 128) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                int arraySize = buf.readUnsignedShort();
                TIntArrayList list = new TIntArrayList();
                for (int j = 0; j < arraySize; j++) {
                    list.add(buf.readInt());
                }
                intArrays.add(list);
            }
        }

        if ((flags & 256) == 256) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                int length = buf.readUnsignedShort();
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                strings.add(new String(bytes, StandardCharsets.UTF_8));
            }
        }

        if ((flags & 512) == 512) {
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                int length = buf.readUnsignedShort();
                NBTTagList list = new NBTTagList();
                for (int j = 0; j < length; j++) {
                    int index = type.readIndex(buf);
                    NBTBase nbt = getTagAt(index);
                    list.appendTag(nbt);
                }
                lists.add(list);
            }
        }

        if ((flags & 1024) == 1024) {
            WrittenType stringType = WrittenType.readType(buf);
            int count = buf.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                int length = buf.readUnsignedShort();
                NBTTagCompound compound = new NBTTagCompound();
                for (int j = 0; j < length; j++) {
                    int stringIndex = stringType.readIndex(buf);
                    String string = strings.get(stringIndex);
                    int objectIndex = type.readIndex(buf);
                    NBTBase nbt = getTagAt(objectIndex);
                    compound.setTag(string, nbt);
                }
                compounds.add(compound);
            }
        }

        if (type != getWrittenType()) {
            throw new IllegalStateException("Differing written types!");
        }
    }

    public WrittenType write(ByteBuf buf) {
        WrittenType type = getWrittenType();

        type.writeType(buf);

        int flags = 0;
        if (!bytes.isEmpty()) flags |= 1;
        if (!shorts.isEmpty()) flags |= 2;
        if (!ints.isEmpty()) flags |= 4;
        if (!longs.isEmpty()) flags |= 8;
        if (!floats.isEmpty()) flags |= 16;
        if (!doubles.isEmpty()) flags |= 32;
        if (!byteArrays.isEmpty()) flags |= 64;
        if (!intArrays.isEmpty()) flags |= 128;
        if (!strings.isEmpty()) flags |= 256;
        if (!lists.isEmpty()) flags |= 512;
        if (!compounds.isEmpty()) flags |= 1024;

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
                byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
                buf.writeShort(bytes.length);
                buf.writeBytes(bytes);
            }
        }

        if (!lists.isEmpty()) {
            NBTSquishDebugging.log(() -> "\nList dictionary size = " + lists.size());
            buf.writeShort(lists.size());
            for (NBTTagList list : lists) {
                int count = list.tagCount();
                ByteBuf normal = Unpooled.buffer();
                ByteBuf packed = Unpooled.buffer();

                packListNormally(normal, type, list);
                packListTightly(packed, type, list);

                if (normal.readableBytes() <= packed.readableBytes()) {
                    buf.writeBytes(normal);
                } else {
                    buf.writeBytes(packed);
                }
            }
        }

        if (!compounds.isEmpty()) {
            WrittenType stringType = WrittenType.getForSize(strings.size());
            stringType.writeType(buf);
            NBTSquishDebugging.log(() -> "\nCompound dictionary size = " + compounds.size());
            buf.writeShort(compounds.size());
            for (NBTTagCompound compound : compounds) {
                buf.writeShort(compound.getSize());
                for (String key : compound.getKeySet()) {
                    stringType.writeIndex(buf, strings.indexOf(key));
                    type.writeIndex(buf, indexOfTag(compound.getTag(key)));
                }
            }
        }

        return type;
    }

    public void packListNormally(ByteBuf buf, WrittenType type, NBTTagList list) {
        buf.writeByte(0);
        buf.writeShort(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            type.writeIndex(buf, indexOfTag(list.get(i)));
        }
    }

    private void packListTightly(ByteBuf buf, WrittenType type, NBTTagList list) {
        buf.writeByte(1);
        int[] data = new int[list.tagCount()];
        TIntIntHashMap indexes = new TIntIntHashMap();
        for (int i = 0; i < list.tagCount(); i++) {
            int index = indexOfTag(list.get(i));
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
        buf.writeShort(entries.size());

        TIntArrayList sortedIndexes = new TIntArrayList();
        for (IndexEntry entry : entries) {
            sortedIndexes.add(entry.index);
        }
        for (IndexEntry entry : entries) {
            type.writeIndex(buf, entry.index);
        }

        TIntArrayList nextData = new TIntArrayList();
        nextData.add(data);
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
            sortedIndexes.remove(0, Math.min(sortedIndexes.size(), maxVal - 1));
            buf.writeBytes(bitset.getBytes());
            nextData = nextNextData;
        }
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
    }

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
            if (!lists.contains(list)) {
                for (int i = 0; i < list.tagCount(); i++) {
                    addTag(list.get(i));
                }
                lists.add(list);
            }
        } else if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            if (!compounds.contains(compound)) {
                for (String key : compound.getKeySet()) {
                    if (!strings.contains(key)) {
                        strings.add(key);
                    }
                    addTag(compound.getTag(key));
                }
                compounds.add(compound);
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
            return ints.indexOf(((NBTTagInt) nbt).getInt());
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
            return offset + lists.indexOf(nbt);
        } else {
            offset += lists.size();
        }
        if (nbt instanceof NBTTagCompound) {
            return offset + compounds.indexOf(nbt);
        }
        throw new IllegalArgumentException("Cannot handle tag " + nbt);
    }

    public NBTBase getTagAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(index + " was less than 0!");
        }
        if (index < bytes.size()) {
            return new NBTTagByte(bytes.get(index));
        } else {
            index -= bytes.size();
        }
        if (index < shorts.size()) {
            return new NBTTagShort(shorts.get(index));
        } else {
            index -= shorts.size();
        }
        throw new IllegalArgumentException("Cannot handle index " + index);
    }

    public int size() {
        // @formatter:off
        return bytes.size()
             + shorts.size()
             + ints.size()
             + longs.size()
             + floats.size()
             + doubles.size()
             + byteArrays.size()
             + intArrays.size()
             + strings.size()
             + lists.size()
             + compounds.size();
        // @formatter:on
    }

    public WrittenType getWrittenType() {
        return WrittenType.getForSize(size());
    }

    public int stringSize() {
        return strings.size();
    }
}
