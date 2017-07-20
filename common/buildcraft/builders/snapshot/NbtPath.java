/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

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

    public NBTBase get(NBTTagByte tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagShort tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagInt tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagLong tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagFloat tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagDouble tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagByteArray tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return null;
            }
            if (key >= 0 && key < tag.getByteArray().length) {
                return new NBTTagByte(tag.getByteArray()[key]);
            } else {
                return null;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagString tag) {
        if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagList tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return null;
            }
            if (key >= 0 && key < tag.tagCount()) {
                return new NbtPath(elements.subList(1, elements.size())).get(tag.get(key));
            } else {
                return null;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagCompound tag) {
        if (!elements.isEmpty()) {
            String key = elements.get(0);
            if (tag.hasKey(key)) {
                return new NbtPath(elements.subList(1, elements.size())).get(tag.getTag(key));
            } else {
                return null;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTTagIntArray tag) {
        if (elements.size() == 1) {
            int key;
            try {
                key = Integer.parseInt(elements.get(0));
            } catch (NumberFormatException e) {
                return null;
            }
            if (key >= 0 && key < tag.getIntArray().length) {
                return new NBTTagInt(tag.getIntArray()[key]);
            } else {
                return null;
            }
        } else if (elements.isEmpty()) {
            return tag;
        } else {
            return null;
        }
    }

    public NBTBase get(NBTBase tag) {
        switch (tag.getId()) {
            case Constants.NBT.TAG_BYTE:
                return get((NBTTagByte) tag);
            case Constants.NBT.TAG_SHORT:
                return get((NBTTagShort) tag);
            case Constants.NBT.TAG_INT:
                return get((NBTTagInt) tag);
            case Constants.NBT.TAG_LONG:
                return get((NBTTagLong) tag);
            case Constants.NBT.TAG_FLOAT:
                return get((NBTTagFloat) tag);
            case Constants.NBT.TAG_DOUBLE:
                return get((NBTTagDouble) tag);
            case Constants.NBT.TAG_BYTE_ARRAY:
                return get((NBTTagByteArray) tag);
            case Constants.NBT.TAG_STRING:
                return get((NBTTagString) tag);
            case Constants.NBT.TAG_LIST:
                return get((NBTTagList) tag);
            case Constants.NBT.TAG_COMPOUND:
                return get((NBTTagCompound) tag);
            case Constants.NBT.TAG_INT_ARRAY:
                return get((NBTTagIntArray) tag);
            default:
                return null;
        }
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
