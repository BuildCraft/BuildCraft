/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.BCLog;

public final class NBTUtils {
    /** Deactivate constructor */
    private NBTUtils() {

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

    public static NBTTagCompound getItemData(ItemStack stack) {
        if (stack == null) {
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
        if (pos == null) return null;
        return new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    public static BlockPos readBlockPos(NBTBase base) {
        if (base == null) return null;
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

    public static NBTBase writeObject(Object obj) {
        if (obj == null) throw new NullPointerException("obj");
        if (obj instanceof Byte) return new NBTTagByte((Byte) obj);
        if (obj instanceof Short) return new NBTTagShort((Short) obj);
        if (obj instanceof Integer) return new NBTTagInt((Integer) obj);
        if (obj instanceof Long) return new NBTTagLong((Long) obj);
        if (obj instanceof Float) return new NBTTagFloat((Float) obj);
        if (obj instanceof Double) return new NBTTagDouble((Double) obj);
        if (obj instanceof Boolean) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("boolean", (Boolean) obj);
            return nbt;
        }
        if (obj instanceof EnumFacing) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("type", "minecraft:enumfacing");
            nbt.setTag("value", writeEnum((EnumFacing) obj));
            return nbt;
        }
        if (obj instanceof Enum<?>) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("type", "enum:" + obj.getClass().getName());
            nbt.setTag("value", writeEnum((Enum) obj));
            return nbt;
        }
        throw new IllegalArgumentException("Cannot write class " + obj.getClass() + " directly to NBT!");
    }

    public static Object readObject(NBTBase nbt) {
        if (nbt == null) return null;
        if (nbt instanceof NBTPrimitive) {
            NBTPrimitive prim = (NBTPrimitive) nbt;
            if (prim instanceof NBTTagByte) return prim.getByte();
            if (prim instanceof NBTTagShort) return prim.getShort();
            if (prim instanceof NBTTagInt) return prim.getInt();
            if (prim instanceof NBTTagLong) return prim.getLong();
            if (prim instanceof NBTTagFloat) return prim.getFloat();
            if (prim instanceof NBTTagDouble) return prim.getDouble();
            else throw new Error("Seriously what? When was a new primitive NBT class added?");
        }
        if (nbt instanceof NBTTagString) return ((NBTTagString) nbt).getString();
        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound comp = (NBTTagCompound) nbt;
            if (comp.hasKey("boolean")) return comp.getBoolean("boolean");
            if (comp.hasKey("type", Constants.NBT.TAG_STRING) && comp.hasKey("value")) {
                String type = ((NBTTagCompound) nbt).getString("type");
                NBTBase valueTag = comp.getTag("value");
                if ("minecraft:enumfacing".equals(type)) return readEnum(valueTag, EnumFacing.class);
                if (type.startsWith("enum:")) try {
                    Class<?> clazz = Class.forName(type.replace("enum:", ""));
                    if (clazz.isEnum()) {
                        return readEnum(valueTag, (Class<Enum>) clazz);
                    } else {
                        BCLog.logger.warn("The type " + type + " refered to a class that was not an enum type!");
                    }
                } catch (ClassNotFoundException e) {
                    BCLog.logger.warn("Tried to load " + type + " but couldn't find the enum class!");
                }
            }
        }
        throw new Error("Tried to load an object from an unknown tag! " + nbt);
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
}
