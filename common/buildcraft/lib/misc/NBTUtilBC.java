/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;

public final class NBTUtilBC {
    /** Deactivate constructor */
    private NBTUtilBC() {

    }

    public static NBTTagCompound load(byte[] data) {
        try {
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(data));
            return nbt;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static NBTTagCompound getItemData(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return new NBTTagCompound();
        }
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        return nbt;
    }

    public static void writeUUID(NBTTagCompound data, String tag, UUID uuid) {
        if (uuid == null) {
            return;
        }
        NBTTagCompound nbtTag = new NBTTagCompound();
        nbtTag.setLong("most", uuid.getMostSignificantBits());
        nbtTag.setLong("least", uuid.getLeastSignificantBits());
        data.setTag(tag, nbtTag);
    }

    public static UUID readUUID(NBTTagCompound data, String tag) {
        if (data.hasKey(tag)) {
            NBTTagCompound nbtTag = data.getCompoundTag(tag);
            return new UUID(nbtTag.getLong("most"), nbtTag.getLong("least"));
        }
        return null;
    }

    public static byte[] save(NBTTagCompound compound) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            CompressedStreamTools.writeCompressed(compound, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static NBTTagIntArray writeBlockPos(BlockPos pos) {
        return new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    public static NBTTagCompound writeBlockPosAsCompound(BlockPos pos) {
        if (pos == null) return null;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", pos.getX());
        nbt.setInteger("y", pos.getX());
        nbt.setInteger("z", pos.getX());
        return nbt;
    }

    public static BlockPos readBlockPos(NBTBase base) {
        if (base == null) return BlockPos.ORIGIN;
        switch (base.getId()) {
            case Constants.NBT.TAG_INT_ARRAY: {
                int[] array = ((NBTTagIntArray) base).getIntArray();
                return new BlockPos(array[0], array[1], array[2]);
            }
            case Constants.NBT.TAG_COMPOUND: {
                NBTTagCompound nbt = (NBTTagCompound) base;
                BlockPos pos = BlockPos.ORIGIN;
                if (nbt.hasKey("i")) {
                    int i = nbt.getInteger("i");
                    int j = nbt.getInteger("j");
                    int k = nbt.getInteger("k");
                    pos = new BlockPos(i, j, k);
                } else if (nbt.hasKey("x")) {
                    int x = nbt.getInteger("x");
                    int y = nbt.getInteger("y");
                    int z = nbt.getInteger("z");
                    pos = new BlockPos(x, y, z);
                } else if (nbt.hasKey("pos")) {
                    return readBlockPos(nbt.getTag("pos"));
                } else {
                    BCLog.logger.warn("Attempted to read a block positions from a compound tag without the correct sub-tags! (" + base + ")", new Throwable());
                }
                return pos;
            }
        }
        BCLog.logger.warn("Attempted to read a block position from an invalid tag! (" + base + ")", new Throwable());
        return BlockPos.ORIGIN;
    }

    public static NBTTagList writeVec3d(Vec3d vec3) {
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagDouble(vec3.xCoord));
        list.appendTag(new NBTTagDouble(vec3.yCoord));
        list.appendTag(new NBTTagDouble(vec3.zCoord));
        return list;
    }

    public static Vec3d readVec3d(NBTBase nbt) {
        if (nbt instanceof NBTTagList) {
            return readVec3d((NBTTagList) nbt);
        }
        return new Vec3d(0, 0, 0);
    }

    public static Vec3d readVec3d(NBTTagCompound nbt, String tagName) {
        return readVec3d(nbt.getTagList(tagName, Constants.NBT.TAG_DOUBLE));
    }

    public static Vec3d readVec3d(NBTTagList list) {
        return new Vec3d(list.getDoubleAt(0), list.getDoubleAt(1), list.getDoubleAt(2));
    }

    private static final String NULL_ENUM_STRING = "_NULL";

    public static <E extends Enum<E>> NBTBase writeEnum(E value) {
        if (value == null) {
            return new NBTTagString(NULL_ENUM_STRING);
        }
        return new NBTTagString(value.name());
    }

    public static <E extends Enum<E>> E readEnum(NBTBase nbt, Class<E> clazz) {
        if (nbt instanceof NBTTagString) {
            String value = ((NBTTagString) nbt).getString();
            if (NULL_ENUM_STRING.equals(value)) {
                return null;
            }
            try {
                return Enum.valueOf(clazz, value);
            } catch (Throwable t) {
                // In case we didn't find the constant
                BCLog.logger.warn("Tried and failed to read the value(" + value + ") from " + clazz.getSimpleName(), t);
                return null;
            }
        } else if (nbt instanceof NBTTagByte) {
            byte value = ((NBTTagByte) nbt).getByte();
            if (value < 0 || value >= clazz.getEnumConstants().length) {
                return null;
            } else {
                return clazz.getEnumConstants()[value];
            }
        } else if (nbt == null) {
            return null;
        } else {
            BCLog.logger.warn(new IllegalArgumentException("Tried to read an enum value when it was not a string! This is probably not good!"));
            return null;
        }
    }

    public static NBTBase writeDoubleArray(double[] data) {
        NBTTagList list = new NBTTagList();
        for (double d : data) {
            list.appendTag(new NBTTagDouble(d));
        }
        return list;
    }

    public static double[] readDoubleArray(NBTBase tag, int intendedLength) {
        double[] arr = new double[intendedLength];
        if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag;
            for (int i = 0; i < list.tagCount() && i < intendedLength; i++) {
                arr[i] = list.getDoubleAt(i);
            }
        }
        return arr;
    }

    public static NBTTagCompound writeEntireBlockState(IBlockState state) {
        if (state == null || state == Blocks.AIR.getDefaultState()) {
            return new NBTTagCompound();
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("block", state.getBlock().getRegistryName().toString());
        nbt.setTag("state", writeBlockStateProperties(state));
        return nbt;
    }

    /** @deprecated Use {@link NBTUtil#readBlockState(NBTTagCompound)} instead! */
    public static IBlockState readEntireBlockState(NBTTagCompound nbt) throws InvalidInputDataException {
        if (nbt.hasNoTags()) {
            return Blocks.AIR.getDefaultState();
        }
        Block block = Block.getBlockFromName(nbt.getString("block"));
        if (block == null || block == Blocks.AIR) {
            throw new InvalidInputDataException("Unknown block " + nbt.getString("block"));
        }
        return readBlockStateProperties(block.getDefaultState(), nbt.getCompoundTag("state"));
    }

    public static NBTTagCompound writeBlockStateProperties(IBlockState state) {
        NBTTagCompound nbt = new NBTTagCompound();
        for (IProperty<?> prop : state.getPropertyKeys()) {
            nbt.setString(prop.getName().toLowerCase(Locale.ROOT), getPropName(state, prop));
        }
        return nbt;
    }

    private static <V extends Comparable<V>> String getPropName(IBlockState state, IProperty<V> prop) {
        return prop.getName(state.getValue(prop)).toLowerCase(Locale.ROOT);
    }

    public static IBlockState readBlockStateProperties(IBlockState state, NBTTagCompound nbt) {
        for (IProperty<?> prop : state.getPropertyKeys()) {
            state = updateState(state, prop, nbt.getString(prop.getName().toLowerCase(Locale.ROOT)));
        }
        return state;
    }

    private static <V extends Comparable<V>> IBlockState updateState(IBlockState state, IProperty<V> prop, String string) {
        for (V val : prop.getAllowedValues()) {
            if (prop.getName(val).equalsIgnoreCase(string)) {
                return state.withProperty(prop, val);
            }
        }
        BCLog.logger.warn("[lib.nbt] Failed to read the state property " + string + " as " + prop);
        return state;
    }

    public static NBTTagCompound writeLocalDateTime(LocalDateTime localDateTime) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("year", localDateTime.getYear());
        nbt.setInteger("month", localDateTime.getMonthValue());
        nbt.setInteger("day", localDateTime.getDayOfMonth());
        nbt.setInteger("hour", localDateTime.getHour());
        nbt.setInteger("minute", localDateTime.getMinute());
        nbt.setInteger("second", localDateTime.getSecond());
        return nbt;
    }

    public static LocalDateTime readLocalDateTime(NBTTagCompound nbt) {
        int year = nbt.getInteger("year");
        int month = nbt.getInteger("month");
        int dayOfMonth = nbt.getInteger("day");
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        int hour = nbt.getInteger("hour");
        int minute = nbt.getInteger("minute");
        int second = nbt.getInteger("second");
        LocalTime time = LocalTime.of(hour, minute, second);
        return LocalDateTime.of(date, time);
    }

    /** Writes an {@link EnumSet} to an {@link NBTBase}. The returned type will either be {@link NBTTagByte} or
     * {@link NBTTagByteArray}.
     * 
     * @param clazz The class that the {@link EnumSet} is of. This is required as we have no way of getting the class
     *            from the set. */
    public static <E extends Enum<E>> NBTBase writeEnumSet(EnumSet<E> set, Class<E> clazz) {
        E[] constants = clazz.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException("Not an enum type " + clazz);
        BitSet bitset = new BitSet();
        for (E e : constants) {
            if (set.contains(e)) {
                bitset.set(e.ordinal());
            }
        }
        byte[] bytes = bitset.toByteArray();
        if (bytes.length == 1) {
            return new NBTTagByte(bytes[0]);
        } else {
            return new NBTTagByteArray(bytes);
        }
    }

    public static <E extends Enum<E>> EnumSet<E> readEnumSet(NBTBase tag, Class<E> clazz) {
        E[] constants = clazz.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException("Not an enum type " + clazz);
        byte[] bytes;
        if (tag instanceof NBTTagByte) {
            bytes = new byte[] { ((NBTTagByte) tag).getByte() };
        } else if (tag instanceof NBTTagByteArray) {
            bytes = ((NBTTagByteArray) tag).getByteArray();
        } else {
            bytes = new byte[] {};
            BCLog.logger.warn("[lib.nbt] Tried to read an enum set from " + tag);
        }
        BitSet bitset = BitSet.valueOf(bytes);
        EnumSet<E> set = EnumSet.noneOf(clazz);
        for (E e : constants) {
            if (bitset.get(e.ordinal())) {
                set.add(e);
            }
        }
        return set;
    }

    public static NBTTagList writeCompoundList(Stream<NBTTagCompound> stream) {
        NBTTagList list = new NBTTagList();
        stream.forEach(list::appendTag);
        return list;
    }

    public static Stream<NBTTagCompound> readCompoundList(NBTBase list) {
        if (list == null) {
            return Stream.empty();
        }
        if (!(list instanceof NBTTagList)) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(0, ((NBTTagList) list).tagCount()).mapToObj(((NBTTagList) list)::getCompoundTagAt);
    }

    public static NBTTagList writeStringList(Stream<String> stream) {
        NBTTagList list = new NBTTagList();
        stream.map(NBTTagString::new).forEach(list::appendTag);
        return list;
    }

    public static Stream<String> readStringList(NBTBase list) {
        if (list == null) {
            return Stream.empty();
        }
        if (!(list instanceof NBTTagList)) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(0, ((NBTTagList) list).tagCount()).mapToObj(((NBTTagList) list)::getStringTagAt);
    }

    public static NBTTagByteArray writeBooleanList(Stream<Boolean> stream) {
        Boolean[] booleans = stream.toArray(Boolean[]::new);
        BitSet bitSet = new BitSet(booleans.length);
        for (int i = 0; i < booleans.length; i++) {
            bitSet.set(i, bitSet.get(i));
        }
        return new NBTTagByteArray(bitSet.toByteArray());
    }

    public static Stream<Boolean> readBooleanList(NBTBase list) {
        if (list == null) {
            return Stream.empty();
        }
        if (!(list instanceof NBTTagByteArray)) {
            throw new IllegalArgumentException();
        }
        BitSet bitSet = BitSet.valueOf(((NBTTagByteArray) list).getByteArray());
        return IntStream.range(0, bitSet.length()).mapToObj(bitSet::get);
    }
}
