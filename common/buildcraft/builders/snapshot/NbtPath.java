/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.List;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;

import net.minecraftforge.common.util.Constants;

import buildcraft.lib.misc.NBTUtilBC;

public class NbtPath {
    private final List<String> elements;

    private NbtPath(List<String> elements) {
        this.elements = elements;
    }

    public NbtElement get(NbtByte tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public NbtElement get(NbtShort tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public NbtElement get(NbtInt tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public NbtElement get(NbtLong tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public NbtElement get(NbtFloat tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public NbtElement get(NbtDouble tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public NbtElement get(NbtByteArray tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return NBTUtilBC.NBT_NULL;
            }
            if (key >= 0 && key < tag.getByteArray().length) {
                return new NbtByte(tag.getByteArray()[key]);
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public NbtElement get(NbtString tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public NbtElement get(NbtList tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return NBTUtilBC.NBT_NULL;
            }
            if (key >= 0 && key < tag.tagCount()) {
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

    public NbtElement get(NbtCompound tag) {
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

    public NbtElement get(NbtIntArray tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return NBTUtilBC.NBT_NULL;
            }
            if (key >= 0 && key < tag.getIntArray().length) {
                return new NbtInt(tag.getIntArray()[key]);
            } else {
                return NBTUtilBC.NBT_NULL;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return NBTUtilBC.NBT_NULL;
        }
    }

    public NbtElement get(NbtElement tag) {
        switch (tag.getId()) {
            case NbtElement.BYTE_TYPE:
                return get((NbtByte) tag);
            case NbtElement.SHORT_TYPE:
                return get((NbtShort) tag);
            case NbtElement.INT_TYPE:
                return get((NbtInt) tag);
            case NbtElement.LONG_TYPE:
                return get((NbtLong) tag);
            case NbtElement.FLOAT_TYPE:
                return get((NbtFloat) tag);
            case NbtElement.DOUBLE_TYPE:
                return get((NbtDouble) tag);
            case NbtElement.BYTE_TYPE_ARRAY:
                return get((NbtByteArray) tag);
            case NbtElement.STRING_TYPE:
                return get((NbtString) tag);
            case NbtElement.LIST_TYPE:
                return get((NbtList) tag);
            case NbtElement.COMPOUND_TYPE:
                return get((NbtCompound) tag);
            case NbtElement.INT_TYPE_ARRAY:
                return get((NbtIntArray) tag);
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
