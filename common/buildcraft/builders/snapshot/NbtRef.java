/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

public class NbtRef<N extends NBTBase> {
    private final NbtPath path;
    private final N value;

    private NbtRef(NbtPath path, N value) {
        this.path = path;
        this.value = value;
    }

    public N get(NBTBase nbt) {
        if (path != null) {
            // noinspection unchecked
            return (N) path.get(nbt);
        }
        if (value != null) {
            return value;
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public static final TypeAdapterFactory TYPE_ADAPTER_FACTORY = new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() != NbtRef.class) {
                return null;
            }
            // noinspection unchecked
            Class<? extends NBTBase> nClass = (Class<? extends NBTBase>)
                ((ParameterizedType) type.getType()).getActualTypeArguments()[0];
            if (nClass == NBTTagByteArray.class || nClass == NBTTagIntArray.class || nClass == NBTTagList.class) {
                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() != JsonToken.BEGIN_ARRAY) {
                            //noinspection unchecked
                            return (T) new NbtRef<>(
                                ((Map<String, NbtPath>) (gson.fromJson(
                                    in,
                                    new TypeToken<Map<String, NbtPath>>() {
                                    }.getType()
                                ))).get("ref"),
                                null
                            );
                        } else {
                            //noinspection unchecked
                            return (T) new NbtRef<>(
                                null,
                                gson.fromJson(in, nClass)
                            );
                        }
                    }
                };
            } else {
                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.BEGIN_ARRAY) {
                            //noinspection unchecked
                            return (T) new NbtRef<>(
                                gson.fromJson(in, NbtPath.class),
                                null
                            );
                        } else {
                            //noinspection unchecked
                            return (T) new NbtRef<>(
                                null,
                                gson.fromJson(in, nClass)
                            );
                        }
                    }
                };
            }
        }
    };
}
