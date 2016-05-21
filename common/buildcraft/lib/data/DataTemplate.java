package buildcraft.lib.data;

import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.common.util.Constants;

/** Helper class for reading and writing NBTTagCompounds to and from packets. Provides automatic compression for
 * booleans */
public class DataTemplate implements IDataTemplate {
    private static final int MAX_STRING_LENGTH = 255;

    private final ImmutableList<String> booleans, bytes, shorts, ints, longs, floats, doubles, strings;
    private final ImmutableList<String> byteArrays, intArrays;
    private final ImmutableMap<String, IDataTemplate> templates, templateArrays;
    private final ImmutableList<String> templateKeys, templateArrayKeys;

    private DataTemplate(Builder builder) {
        booleans = sort(builder.booleans.build());
        bytes = sort(builder.bytes.build());
        shorts = sort(builder.shorts.build());
        ints = sort(builder.ints.build());
        longs = sort(builder.longs.build());
        floats = sort(builder.floats.build());
        doubles = sort(builder.doubles.build());
        strings = sort(builder.strings.build());

        byteArrays = sort(builder.byteArrays.build());
        intArrays = sort(builder.intArrays.build());

        templates = builder.templates.build();
        templateArrays = builder.templateArrays.build();

        templateKeys = sort(templates.keySet());
        templateArrayKeys = sort(templateArrays.keySet());
    }

    private static ImmutableList<String> sort(Set<String> set) {
        List<String> strings = new ArrayList<>();
        for (String s : set) {
            strings.add(s.toLowerCase(Locale.ROOT));
        }
        Collections.sort(strings);
        return ImmutableList.copyOf(strings);
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        // Disable formatting for this: lots of repetition
        // @formatter:off
        for (String s : booleans) builder.addBoolean(s);
        for (String s : bytes) builder.addByte(s);
        for (String s : shorts) builder.addShort(s);
        for (String s : ints) builder.addInt(s);
        for (String s : longs) builder.addLong(s);
        for (String s : floats) builder.addFloat(s);
        for (String s : doubles) builder.addDouble(s);
        for (String s : strings) builder.addString(s);
        for (String s : byteArrays) builder.addByteArray(s);
        for (String s : intArrays) builder.addIntArray(s);
        for (String s : templateKeys) builder.addTemplate(s, templates.get(s));
        for (String s : templateArrayKeys) builder.addTemplate(s, templateArrays.get(s));
        // @formatter:on

        return builder;
    }

    @Override
    public NBTTagCompound readFromPacketBuffer(PacketBuffer buffer) {
        NBTTagCompound nbt = new NBTTagCompound();

        // Disable formatting for this: lots of repetition
        // @formatter:off
        for (String s : bytes) nbt.setByte(s, buffer.readByte());
        for (String s : shorts) nbt.setShort(s, buffer.readShort());
        for (String s : ints) nbt.setInteger(s, buffer.readInt());
        for (String s : longs) nbt.setLong(s, buffer.readLong());
        for (String s : floats) nbt.setFloat(s, buffer.readFloat());
        for (String s : doubles) nbt.setDouble(s, buffer.readDouble());
        for (String s : strings) nbt.setString(s, buffer.readStringFromBuffer(MAX_STRING_LENGTH));
        for (String s : byteArrays) nbt.setByteArray(s, buffer.readByteArray());
        for (String s : intArrays) nbt.setIntArray(s, buffer.readVarIntArray());
        // @formatter:on

        // booleans
        for (int i = 0; i < booleans.size() / 8; i++) {
            int max = Math.min(booleans.size(), i * 8 + 7);
            List<String> subList = booleans.subList(i * 8, max);
            short all = buffer.readUnsignedByte();
            for (int j = 0; j < max; j++) {
                boolean val = 1 == ((all >> j) & 1);
                nbt.setBoolean(subList.get(j), val);
            }
        }

        for (String s : templateKeys) {
            IDataTemplate tmpl = templates.get(s);
            nbt.setTag(s, tmpl.readFromPacketBuffer(buffer));
        }

        for (String s : templateArrayKeys) {
            IDataTemplate tmpl = templateArrays.get(s);
            int count = buffer.readShort();
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < count; i++) {
                list.appendTag(tmpl.readFromPacketBuffer(buffer));
            }
            nbt.setTag(s, list);
        }

        return nbt;
    }

    @Override
    public void writeToPacketBuffer(NBTBase base, PacketBuffer buffer) {
        NBTTagCompound nbt = base instanceof NBTTagCompound ? (NBTTagCompound) base : new NBTTagCompound();
        // Disable formatting for this: lots of repetition
        // @formatter:off
        for (String s : bytes) buffer.writeByte(nbt.getByte(s));
        for (String s : shorts) buffer.writeShort(nbt.getShort(s));
        for (String s : ints) buffer.writeInt(nbt.getInteger(s));
        for (String s : longs) buffer.writeLong(nbt.getLong(s));
        for (String s : floats) buffer.writeFloat(nbt.getFloat(s));
        for (String s : doubles) buffer.writeDouble(nbt.getDouble(s));
        for (String s : byteArrays) buffer.writeByteArray(nbt.getByteArray(s));
        for (String s : intArrays) buffer.writeVarIntArray(nbt.getIntArray(s));
        // @formatter:on

        for (int i = 0; i < booleans.size() / 8; i++) {
            int max = Math.min(booleans.size(), i * 8 + 7);
            List<String> subList = booleans.subList(i * 8, max);
            short all = 0;
            for (int j = 0; j < max; j++) {
                int v = nbt.getBoolean(subList.get(j)) ? 1 : 0;
                all |= v << j;
            }
            buffer.writeByte(all);
        }

        for (String s : templateKeys) {
            IDataTemplate tmpl = templates.get(s);
            tmpl.writeToPacketBuffer(nbt.getTag(s), buffer);
        }

        for (String s : templateArrayKeys) {
            IDataTemplate tmpl = templateArrays.get(s);
            NBTTagList list = (NBTTagList) nbt.getTag(s);
            for (int i = 0; i < list.tagCount(); i++) {
                tmpl.writeToPacketBuffer(list.get(i), buffer);
            }
        }
    }

    public static class Builder {
        private final Set<String> takenKeys = new HashSet<>();
        private final ImmutableSet.Builder<String> booleans, bytes, shorts, ints, longs, floats, doubles, strings;
        private final ImmutableSet.Builder<String> byteArrays, intArrays;
        private final ImmutableMap.Builder<String, IDataTemplate> templates, templateArrays;

        public Builder() {
            booleans = ImmutableSet.builder();
            bytes = ImmutableSet.builder();
            shorts = ImmutableSet.builder();
            ints = ImmutableSet.builder();
            longs = ImmutableSet.builder();
            floats = ImmutableSet.builder();
            doubles = ImmutableSet.builder();
            strings = ImmutableSet.builder();

            byteArrays = ImmutableSet.builder();
            intArrays = ImmutableSet.builder();

            templates = ImmutableMap.builder();
            templateArrays = ImmutableMap.builder();
        }

        private void checkKey(String key) {
            if (takenKeys.contains(key)) {
                throw new IllegalArgumentException("Duplicate key " + key);
            }
            takenKeys.add(key);
        }

        // Disable formatting for this: lots of repetition
        // @formatter:off
        public Builder addBoolean(String key) { checkKey(key); booleans.add(key); return this; }
        public Builder addByte(String key) { checkKey(key); bytes.add(key); return this; }
        public Builder addShort(String key) { checkKey(key); shorts.add(key); return this; }
        public Builder addInt(String key) { checkKey(key); ints.add(key); return this; }
        public Builder addLong(String key) { checkKey(key); longs.add(key); return this; }
        public Builder addFloat(String key) { checkKey(key); floats.add(key); return this; }
        public Builder addDouble(String key) { checkKey(key); doubles.add(key); return this; }
        public Builder addString(String key) { checkKey(key); strings.add(key); return this; }
        public Builder addByteArray(String key) { checkKey(key); byteArrays.add(key); return this; }
        public Builder addIntArray(String key) { checkKey(key); intArrays.add(key); return this; }
        public Builder addTemplate(String key, IDataTemplate tmpl) { checkKey(key); templates.put(key, tmpl); return this; }
        public Builder addTemplateArray(String key, IDataTemplate tmpl) { checkKey(key); templateArrays.put(key, tmpl); return this; }
        public Builder addBlockPos(String key) { return addTemplate(key, BlockPosTemplate.INSTANCE); };
        public Builder addVec3d(String key) { return addTemplate(key, Vec3dTemplate.INSTANCE); };
        public <E extends Enum<E>> Builder addEnum(String key, Class<E> clazz) { return addTemplate(key, EnumTemplate.getForEnum(clazz)); }
        // @formatter:on

        public DataTemplate build() {
            return new DataTemplate(this);
        }
    }
}
