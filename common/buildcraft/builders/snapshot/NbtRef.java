/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;

public class NbtRef<N extends INBT> {
    private final EnumType type;
    private final NbtPath path;
    private final N value;

    private NbtRef(EnumType type, NbtPath path, N value) {
        this.type = type;
        this.path = path;
        this.value = value;
    }

    public Optional<N> get(INBT nbt) {
        if (type == EnumType.BY_PATH) {
            // noinspection unchecked
            return NBTUtilBC.toOptional((N) path.get(nbt));
        }
        if (type == EnumType.BY_VALUE) {
            return NBTUtilBC.toOptional(value);
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString() {
        if (type == EnumType.BY_PATH) {
            return "NbtRef{path=" + path + "}";
        }
        if (type == EnumType.BY_VALUE) {
            return "NbtRef{value=" + value + "}";
        }
        throw new IllegalStateException();
    }

    @SuppressWarnings("WeakerAccess")
    public static final TypeAdapterFactory TYPE_ADAPTER_FACTORY = new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() != NbtRef.class) {
                return null;
            }
            // noinspection unchecked
            Class<? extends INBT> nClass = (Class<? extends INBT>)
                    ((ParameterizedType) type.getType()).getActualTypeArguments()[0];
            if (nClass == ByteArrayNBT.class || nClass == IntArrayNBT.class || nClass == ListNBT.class) {
                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() != JsonToken.BEGIN_ARRAY) {
                            // noinspection unchecked
                            return (T) EnumType.BY_PATH.create(
                                    ((Map<String, NbtPath>) (gson.fromJson(
                                            in,
                                            new TypeToken<Map<String, NbtPath>>() {
                                            }.getType()
                                    ))).get("ref")
                            );
                        } else {
                            // noinspection unchecked
                            return (T) EnumType.BY_VALUE.create(
                                    gson.<INBT>fromJson(in, nClass)
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
                            // noinspection unchecked
                            return (T) EnumType.BY_PATH.create(
                                    gson.<NbtPath>fromJson(in, NbtPath.class)
                            );
                        } else {
                            // noinspection unchecked
                            return (T) EnumType.BY_VALUE.create(
                                    gson.<INBT>fromJson(in, nClass)
                            );
                        }
                    }
                };
            }
        }
    };

    public enum EnumType {
        BY_PATH {
            @Override
            public NbtRef<?> create(NbtPath path) {
                return new NbtRef<>(this, path, null);
            }
        },
        BY_VALUE {
            @Override
            public <N extends INBT> NbtRef<N> create(N value) {
                return new NbtRef<>(this, null, value);
            }
        };

        public NbtRef<?> create(NbtPath path) {
            throw new UnsupportedOperationException();
        }

        public <N extends INBT> NbtRef<N> create(N value) {
            throw new UnsupportedOperationException();
        }
    }
}
