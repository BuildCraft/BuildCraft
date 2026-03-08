/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class NbtPath {
    private final List<String> elements;

    private NbtPath(List<String> elements) {
        this.elements = elements;
    }

    public INBT get(ByteNBT tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(ShortNBT tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(IntNBT tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(LongNBT tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(FloatNBT tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(DoubleNBT tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(ByteArrayNBT tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return NBTUtilBC.NBT_NULL;
            }
            if (key >= 0 && key < tag.getAsByteArray().length) {
                return ByteNBT.valueOf(tag.getAsByteArray()[key]);
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(StringNBT tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(ListNBT tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return NBTUtilBC.NBT_NULL;
            }
            if (key >= 0 && key < tag.size()) {
                return new NbtPath(elements.subList(1, elements.size())).get(tag.get(key));
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(CompoundNBT tag) {
        if (!elements.isEmpty()) {
            String key = elements.get(0);
            if (tag.contains(key)) {
                return new NbtPath(elements.subList(1, elements.size())).get(tag.get(key));
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else {
            return tag;
        }
    }

    public INBT get(IntArrayNBT tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return NBTUtilBC.NBT_NULL;
            }
            if (key >= 0 && key < tag.getAsIntArray().length) {
                return IntNBT.valueOf(tag.getAsIntArray()[key]);
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public INBT get(INBT tag) {
        switch (tag.getId()) {
            case Constants.NBT.TAG_BYTE:
                return get((ByteNBT) tag);
            case Constants.NBT.TAG_SHORT:
                return get((ShortNBT) tag);
            case Constants.NBT.TAG_INT:
                return get((IntNBT) tag);
            case Constants.NBT.TAG_LONG:
                return get((LongNBT) tag);
            case Constants.NBT.TAG_FLOAT:
                return get((FloatNBT) tag);
            case Constants.NBT.TAG_DOUBLE:
                return get((DoubleNBT) tag);
            case Constants.NBT.TAG_BYTE_ARRAY:
                return get((ByteArrayNBT) tag);
            case Constants.NBT.TAG_STRING:
                return get((StringNBT) tag);
            case Constants.NBT.TAG_LIST:
                return get((ListNBT) tag);
            case Constants.NBT.TAG_COMPOUND:
                return get((CompoundNBT) tag);
            case Constants.NBT.TAG_INT_ARRAY:
                return get((IntArrayNBT) tag);
            default:
                return NBTUtilBC.NBT_NULL;
        }
    }

    @Override
    public String toString() {
        return "NbtPath{" + elements + "}";
    }

    @SuppressWarnings("WeakerAccess")
    public static final JsonDeserializer<NbtPath> DESERIALIZER = (json, typeOfT, context) ->
            new NbtPath(
                    context.deserialize(
                            json,
                            new TypeToken<List<String>>() {
                            }.getType()
                    )
            );
}
