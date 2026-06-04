/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.misc;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.BCLog;

// Ported to Fabric 1.20.1 by R.Chen: all 1.12.2 NBT type names (NbtCompound → NbtCompound, etc.)
// replaced with their Yarn 1.20.1 equivalents; Forge Constants.NBT.* replaced with NbtElement.*_TYPE constants.
public final class NBTUtilBC {
    @SuppressWarnings("WeakerAccess")
    public static final NbtCompound NBT_NULL = new NbtCompound();

    public static <N extends NbtElement> Optional<N> toOptional(N value) {
        return value == NBTUtilBC.NBT_NULL ? Optional.empty() : Optional.of(value);
    }

    public static NbtElement merge(NbtElement destination, NbtElement source) {
        if (source == null) {
            return null;
        }
        if (destination == null) {
            return source;
        }
        if (destination.getType() == NbtElement.COMPOUND_TYPE && source.getType() == NbtElement.COMPOUND_TYPE) {
            NbtCompound result = new NbtCompound();
            for (String key : Sets.union(
                ((NbtCompound) destination).getKeys(),
                ((NbtCompound) source).getKeys()
            )) {
                if (!((NbtCompound) source).contains(key)) {
                    result.put(key, ((NbtCompound) destination).get(key));
                } else if (((NbtCompound) source).get(key) != NBT_NULL) {
                    if (!((NbtCompound) destination).contains(key)) {
                        result.put(key, ((NbtCompound) source).get(key));
                    } else {
                        result.put(
                            key,
                            merge(
                                ((NbtCompound) destination).get(key),
                                ((NbtCompound) source).get(key)
                            )
                        );
                    }
                }
            }
            return result;
        }
        return source;
    }

    public static NbtCompound getItemData(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return new NbtCompound();
        }
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            nbt = new NbtCompound();
            stack.setNbt(nbt);
        }
        return nbt;
    }

    public static NbtIntArray writeBlockPos(BlockPos pos) {
        if (pos == null) {
            throw new NullPointerException("Cannot return a null NBTTag -- pos was null!");
        }
        return new NbtIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    @SuppressWarnings("unused")
    public static NbtCompound writeBlockPosAsCompound(BlockPos pos) {
        if (pos == null) {
            throw new NullPointerException("Cannot return a null NBTTag -- pos was null!");
        }
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        return nbt;
    }

    @Nullable
    public static BlockPos readBlockPos(NbtElement base) {
        if (base == null) {
            return null;
        }
        switch (base.getType()) {
            case NbtElement.INT_ARRAY_TYPE: {
                int[] array = ((NbtIntArray) base).getIntArray();
                if (array.length == 3) {
                    return new BlockPos(array[0], array[1], array[2]);
                }
                return null;
            }
            case NbtElement.COMPOUND_TYPE: {
                NbtCompound nbt = (NbtCompound) base;
                BlockPos pos = null;
                if (nbt.contains("i")) {
                    int i = nbt.getInt("i");
                    int j = nbt.getInt("j");
                    int k = nbt.getInt("k");
                    pos = new BlockPos(i, j, k);
                } else if (nbt.contains("x")) {
                    int x = nbt.getInt("x");
                    int y = nbt.getInt("y");
                    int z = nbt.getInt("z");
                    pos = new BlockPos(x, y, z);
                } else if (nbt.contains("pos")) {
                    return readBlockPos(nbt.get("pos"));
                } else {
                    BCLog.logger.warn("Attempted to read a block positions from a compound tag without the correct sub-tags! (" + base + ")", new Throwable());
                }
                return pos;
            }
        }
        BCLog.logger.warn("Attempted to read a block position from an invalid tag! (" + base + ")", new Throwable());
        return null;
    }

    public static NbtList writeVec3d(Vec3d vec3) {
        NbtList list = new NbtList();
        list.add(NbtDouble.of(vec3.x));
        list.add(NbtDouble.of(vec3.y));
        list.add(NbtDouble.of(vec3.z));
        return list;
    }

    @Nullable
    public static Vec3d readVec3d(NbtElement nbt) {
        if (nbt instanceof NbtList) {
            return readVec3d((NbtList) nbt);
        }
        return null;
    }

    public static Vec3d readVec3d(NbtList list) {
        return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }

    private static final String NULL_ENUM_STRING = "_NULL";

    public static <E extends Enum<E>> NbtElement writeEnum(E value) {
        if (value == null) {
            return NbtString.of(NULL_ENUM_STRING);
        }
        return NbtString.of(value.name());
    }

    public static <E extends Enum<E>> E readEnum(NbtElement nbt, Class<E> clazz) {
        if (nbt instanceof NbtString) {
            String value = nbt.asString();
            if (NULL_ENUM_STRING.equals(value)) {
                return null;
            }
            try {
                return Enum.valueOf(clazz, value);
            } catch (Throwable t) {
                BCLog.logger.warn("Tried and failed to read the value(" + value + ") from " + clazz.getSimpleName(), t);
                return null;
            }
        } else if (nbt instanceof NbtByte) {
            byte value = ((NbtByte) nbt).byteValue();
            if (value < 0 || value >= clazz.getEnumConstants().length) {
                return null;
            } else {
                return clazz.getEnumConstants()[value];
            }
        } else if (nbt == null) {
            return null;
        } else {
            BCLog.logger.warn("Tried to read an enum value when it was not a string! This is probably not good!", new IllegalArgumentException());
            return null;
        }
    }

    public static NbtElement writeDoubleArray(double[] data) {
        NbtList list = new NbtList();
        for (double d : data) {
            list.add(NbtDouble.of(d));
        }
        return list;
    }

    public static double[] readDoubleArray(NbtElement tag, int intendedLength) {
        double[] arr = new double[intendedLength];
        if (tag instanceof NbtList) {
            NbtList list = (NbtList) tag;
            for (int i = 0; i < list.size() && i < intendedLength; i++) {
                arr[i] = list.getDouble(i);
            }
        }
        return arr;
    }

    /** Writes an {@link EnumSet} to an {@link NbtElement}. The returned type will either be {@link NbtByte} or
     * {@link NbtByteArray}.
     *
     * @param clazz The class that the {@link EnumSet} is of. This is required as we have no way of getting the class
     *            from the set. */
    public static <E extends Enum<E>> NbtElement writeEnumSet(EnumSet<E> set, Class<E> clazz) {
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
            return NbtByte.of(bytes[0]);
        } else {
            return new NbtByteArray(bytes);
        }
    }

    public static <E extends Enum<E>> EnumSet<E> readEnumSet(NbtElement tag, Class<E> clazz) {
        E[] constants = clazz.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException("Not an enum type " + clazz);
        byte[] bytes;
        if (tag instanceof NbtByte) {
            bytes = new byte[] { ((NbtByte) tag).byteValue() };
        } else if (tag instanceof NbtByteArray) {
            bytes = ((NbtByteArray) tag).getByteArray();
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

    public static NbtList writeCompoundList(Stream<NbtCompound> stream) {
        NbtList list = new NbtList();
        stream.forEach(list::add);
        return list;
    }

    public static Stream<NbtCompound> readCompoundList(NbtElement listEl) {
        if (listEl == null) {
            return Stream.empty();
        }
        if (!(listEl instanceof NbtList)) {
            throw new IllegalArgumentException();
        }
        NbtList list = (NbtList) listEl;
        return IntStream.range(0, list.size()).mapToObj(list::getCompound);
    }

    public static NbtList writeStringList(Stream<String> stream) {
        NbtList list = new NbtList();
        stream.map(NbtString::of).forEach(list::add);
        return list;
    }

    public static Stream<String> readStringList(NbtElement listEl) {
        if (listEl == null) {
            return Stream.empty();
        }
        if (!(listEl instanceof NbtList)) {
            throw new IllegalArgumentException();
        }
        NbtList list = (NbtList) listEl;
        return IntStream.range(0, list.size()).mapToObj(list::getString);
    }
}
