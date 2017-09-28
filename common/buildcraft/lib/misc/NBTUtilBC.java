/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.BCLog;

public final class NBTUtilBC {
    /** Deactivate constructor */
    private NBTUtilBC() {
    }

    public static NBTBase merge(NBTBase destination, NBTBase source) {
        if (source == null) {
            return null;
        }
        if (destination == null) {
            return source;
        }
        if (destination.getId() == Constants.NBT.TAG_COMPOUND && source.getId() == Constants.NBT.TAG_COMPOUND) {
            NBTTagCompound result = new NBTTagCompound();
            for (String key : Sets.union(
                ((NBTTagCompound) destination).getKeySet(),
                ((NBTTagCompound) source).getKeySet()
            )) {
                if (!((NBTTagCompound) source).hasKey(key)) {
                    result.setTag(key, ((NBTTagCompound) destination).getTag(key));
                } else if (((NBTTagCompound) source).getTag(key) != null) {
                    if (!((NBTTagCompound) destination).hasKey(key)) {
                        result.setTag(key, ((NBTTagCompound) source).getTag(key));
                    } else {
                        result.setTag(
                            key,
                            merge(
                                ((NBTTagCompound) destination).getTag(key),
                                ((NBTTagCompound) source).getTag(key)
                            )
                        );
                    }
                }
            }
            return result;
        }
        return source;
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

    public static NBTTagIntArray writeBlockPos(BlockPos pos) {
        if (pos == null) {
            throw new NullPointerException("Cannot return a null NBTTag -- pos was null!");
        }
        return new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    @SuppressWarnings("unused")
    public static NBTTagCompound writeBlockPosAsCompound(BlockPos pos) {
        if (pos == null) {
            throw new NullPointerException("Cannot return a null NBTTag -- pos was null!");
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", pos.getX());
        nbt.setInteger("y", pos.getY());
        nbt.setInteger("z", pos.getZ());
        return nbt;
    }

    @Nullable
    public static BlockPos readBlockPos(NBTBase base) {
        if (base == null) {
            return null;
        }
        switch (base.getId()) {
            case Constants.NBT.TAG_INT_ARRAY: {
                int[] array = ((NBTTagIntArray) base).getIntArray();
                if (array.length == 3){
                    return new BlockPos(array[0], array[1], array[2]);
                }
                return null;
            }
            case Constants.NBT.TAG_COMPOUND: {
                NBTTagCompound nbt = (NBTTagCompound) base;
                BlockPos pos = null;
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
        return null;
    }

    public static NBTTagList writeVec3d(Vec3d vec3) {
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagDouble(vec3.xCoord));
        list.appendTag(new NBTTagDouble(vec3.yCoord));
        list.appendTag(new NBTTagDouble(vec3.zCoord));
        return list;
    }

    @Nullable
    public static Vec3d readVec3d(NBTBase nbt) {
        if (nbt instanceof NBTTagList) {
            return readVec3d((NBTTagList) nbt);
        }
        return null;
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
}
