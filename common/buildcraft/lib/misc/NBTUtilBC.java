/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BCLog;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class NBTUtilBC {
    @SuppressWarnings("WeakerAccess")
    public static final CompoundNBT NBT_NULL = new CompoundNBT();

    public static <N> Optional<N> toOptional(N value) {
        return value == NBTUtilBC.NBT_NULL ? Optional.empty() : Optional.of(value);
    }

    public static INBT merge(INBT destination, INBT source) {
        if (source == null) {
            return null;
        }
        if (destination == null) {
            return source;
        }
        if (destination.getId() == Constants.NBT.TAG_COMPOUND && source.getId() == Constants.NBT.TAG_COMPOUND) {
            CompoundNBT result = new CompoundNBT();
            for (String key : Sets.union(
                    ((CompoundNBT) destination).getAllKeys(),
                    ((CompoundNBT) source).getAllKeys()
            )) {
                if (!((CompoundNBT) source).contains(key)) {
                    result.put(key, ((CompoundNBT) destination).get(key));
                } else if (((CompoundNBT) source).get(key) != NBT_NULL) {
                    if (!((CompoundNBT) destination).contains(key)) {
                        result.put(key, ((CompoundNBT) source).get(key));
                    } else {
                        result.put(
                                key,
                                merge(
                                        ((CompoundNBT) destination).get(key),
                                        ((CompoundNBT) source).get(key)
                                )
                        );
                    }
                }
            }
            return result;
        }
        return source;
    }

    public static CompoundNBT getItemData(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return new CompoundNBT();
        }
        CompoundNBT nbt = stack.getTag();
        if (nbt == null) {
            nbt = new CompoundNBT();
            stack.setTag(nbt);
        }
        return nbt;
    }

    public static IntArrayNBT writeBlockPos(BlockPos pos) {
        if (pos == null) {
            throw new NullPointerException("Cannot return a null NBTTag -- pos was null!");
        }
        return new IntArrayNBT(new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    @SuppressWarnings("unused")
    public static CompoundNBT writeBlockPosAsCompound(BlockPos pos) {
        if (pos == null) {
            throw new NullPointerException("Cannot return a null NBTTag -- pos was null!");
        }
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        return nbt;
    }

    @Nullable
    public static BlockPos readBlockPos(INBT base) {
        if (base == null) {
            return null;
        }
        switch (base.getId()) {
            case Constants.NBT.TAG_INT_ARRAY: {
                int[] array = ((IntArrayNBT) base).getAsIntArray();
                if (array.length == 3) {
                    return new BlockPos(array[0], array[1], array[2]);
                }
                return null;
            }
            case Constants.NBT.TAG_COMPOUND: {
                CompoundNBT nbt = (CompoundNBT) base;
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

    public static ListNBT writeVec3d(Vector3d vec3) {
        ListNBT list = new ListNBT();
        list.add(DoubleNBT.valueOf(vec3.x));
        list.add(DoubleNBT.valueOf(vec3.y));
        list.add(DoubleNBT.valueOf(vec3.z));
        return list;
    }

    @Nullable
    public static Vector3d readVec3d(INBT nbt) {
        if (nbt instanceof ListNBT) {
            return readVec3d((ListNBT) nbt);
        }
        return null;
    }

    public static Vector3d readVec3d(ListNBT list) {
        return new Vector3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }

    private static final String NULL_ENUM_STRING = "_NULL";

    public static <E extends Enum<E>> INBT writeEnum(E value) {
        if (value == null) {
            return StringNBT.valueOf(NULL_ENUM_STRING);
        }
        return StringNBT.valueOf(value.name());
    }

    public static <E extends Enum<E>> E readEnum(INBT nbt, Class<E> clazz) {
        if (nbt instanceof StringNBT) {
            String value = ((StringNBT) nbt).getAsString();
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
        } else if (nbt instanceof ByteNBT) {
            byte value = ((ByteNBT) nbt).getAsByte();
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

    public static ListNBT writeDoubleArray(double[] data) {
        ListNBT list = new ListNBT();
        for (double d : data) {
            list.add(DoubleNBT.valueOf(d));
        }
        return list;
    }

    // Calen Add
    public static ListNBT writeBooleanArray(boolean[] data) {
        ListNBT list = new ListNBT();
        for (boolean d : data) {
            list.add(IntNBT.valueOf(d ? 1 : 0));
        }
        return list;
    }

    public static double[] readDoubleArray(INBT tag, int intendedLength) {
        double[] arr = new double[intendedLength];
        if (tag instanceof ListNBT) {
            ListNBT list = (ListNBT) tag;
            for (int i = 0; i < list.size() && i < intendedLength; i++) {
                arr[i] = list.getDouble(i);
            }
        }
        return arr;
    }

    public static boolean[] readBooleanArray(ListNBT tag) {
        boolean[] arr = new boolean[tag.size()];
        for (int i = 0; i < tag.size(); i++) {
            arr[i] = tag.getInt(i) != 0;
        }
        return arr;
    }

    /** Writes an {@link EnumSet} to an {@link INBT}. The returned type will either be {@link ByteNBT} or
     * {@link ByteArrayNBT}.
     *
     * @param clazz The class that the {@link EnumSet} is of. This is required as we have no way of getting the class
     *            from the set. */
    public static <E extends Enum<E>> INBT writeEnumSet(EnumSet<E> set, Class<E> clazz) {
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
            return ByteNBT.valueOf(bytes[0]);
        } else {
            return new ByteArrayNBT(bytes);
        }
    }

    public static <E extends Enum<E>> EnumSet<E> readEnumSet(INBT tag, Class<E> clazz) {
        E[] constants = clazz.getEnumConstants();
        if (constants == null) throw new IllegalArgumentException("Not an enum type " + clazz);
        byte[] bytes;
        if (tag instanceof ByteNBT) {
            bytes = new byte[] { ((ByteNBT) tag).getAsByte() };
        } else if (tag instanceof ByteArrayNBT) {
            bytes = ((ByteArrayNBT) tag).getAsByteArray();
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

    public static ListNBT writeCompoundList(Stream<CompoundNBT> stream) {
        ListNBT list = new ListNBT();
        stream.forEach(list::add);
        return list;
    }

    public static Stream<CompoundNBT> readCompoundList(INBT list) {
        if (list == null) {
            return Stream.empty();
        }
        if (!(list instanceof ListNBT)) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(0, ((ListNBT) list).size()).mapToObj(((ListNBT) list)::getCompound);
    }

    public static ListNBT writeStringList(Stream<String> stream) {
        ListNBT list = new ListNBT();
        stream.map(StringNBT::valueOf).forEach(list::add);
        return list;
    }

    public static Stream<String> readStringList(INBT list) {
        if (list == null) {
            return Stream.empty();
        }
        if (!(list instanceof ListNBT)) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(0, ((ListNBT) list).size()).mapToObj(((ListNBT) list)::getString);
    }
}
