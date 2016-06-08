package buildcraft.lib.nbt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.*;

import gnu.trove.list.array.*;
import io.netty.buffer.ByteBuf;

/** Defines a map of commonly used tags. */
public class NBTSquishMap {
    // I'm not completly convinced that this one is necessary.
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
        int numStrings = buf.readInt();
        if (numStrings < 0) throw new IOException("Cannot have less than 0 strings!");
        for (int i = 0; i < numStrings; i++) {
            int length = buf.readUnsignedShort();
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            strings.add(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    public void write(ByteBuf buf) {
        // Sort strings beforehand. I don't know if this makes a difference or not, but it might help gzip to compress
        // Similar strings more (as they are closer).
        Collections.sort(strings);
        buf.writeInt(strings.size());
        for (String s : strings) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            buf.writeShort(bytes.length);
            buf.writeBytes(bytes);
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
                lists.add(list);
                for (int i = 0; i < list.tagCount(); i++) {
                    addTag(list.get(i));
                }
            }
        } else if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            if (!compounds.contains(compound)) {
                compounds.add(compound);
                for (String key : compound.getKeySet()) {
                    if (!strings.contains(key)) {
                        strings.add(key);
                    }
                    addTag(compound.getTag(key));
                }
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
}
