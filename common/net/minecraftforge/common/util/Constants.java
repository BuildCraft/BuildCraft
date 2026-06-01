// STUB(R.Chen): Forge Constants — compile shim. NBT type IDs → use net.minecraft.nbt.NbtElement constants instead.
package net.minecraftforge.common.util;

import net.minecraft.nbt.NbtElement;

public class Constants {
    public static class NBT {
        public static final byte TAG_END = NbtElement.END_TYPE;
        public static final byte TAG_BYTE = NbtElement.BYTE_TYPE;
        public static final byte TAG_SHORT = NbtElement.SHORT_TYPE;
        public static final byte TAG_INT = NbtElement.INT_TYPE;
        public static final byte TAG_LONG = NbtElement.LONG_TYPE;
        public static final byte TAG_FLOAT = NbtElement.FLOAT_TYPE;
        public static final byte TAG_DOUBLE = NbtElement.DOUBLE_TYPE;
        public static final byte TAG_BYTE_ARRAY = NbtElement.BYTE_ARRAY_TYPE;
        public static final byte TAG_STRING = NbtElement.STRING_TYPE;
        public static final byte TAG_LIST = NbtElement.LIST_TYPE;
        public static final byte TAG_COMPOUND = NbtElement.COMPOUND_TYPE;
        public static final byte TAG_INT_ARRAY = NbtElement.INT_ARRAY_TYPE;
        public static final byte TAG_LONG_ARRAY = NbtElement.LONG_ARRAY_TYPE;
        public static final byte TAG_ANY_NUMERIC = 99;
    }
}
