/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import ct.buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class NbtRef<N extends Tag> {
    private final EnumType type;
    private final NbtPath path;
    private final N value;

    private NbtRef(EnumType type, NbtPath path, N value) {
        this.type = type;
        this.path = path;
        this.value = value;
    }

    public Optional<N> get(Tag nbt) {
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
            @SuppressWarnings("unchecked")
			Class<? extends Tag> nClass = (Class<? extends Tag>)
                ((ParameterizedType) type.getType()).getActualTypeArguments()[0];
            if (nClass == ByteArrayTag.class || nClass == IntArrayTag.class || nClass == ListTag.class) {
                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    @SuppressWarnings("unchecked")
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
                                gson.<Tag>fromJson(in, nClass)
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

                    @SuppressWarnings("unchecked")
					@Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.BEGIN_ARRAY) {
                            // noinspection unchecked
                            return (T) EnumType.BY_PATH.create(
                                gson.<NbtPath>fromJson(in, NbtPath.class)
                            );
                        } else {
                            // noinspection unchecked
                        	//var car = gson.<String>fromJson(in, new String().getClass());
                            return (T) EnumType.BY_VALUE.create(
                                gson.<Tag>fromJson(in, nClass)
                            );
                        }
                    }
                };
            }
        }
    };
    
    public static final TypeAdapterFactory NBT_TYPE_ADAPTER_FACTORY = new TypeAdapterFactory() {
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if(type.getRawType() == StringTag.class){
	            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
	            return new TypeAdapter<T>() {
	                @Override
	                public void write(JsonWriter out, T value) throws IOException {
	                    throw new UnsupportedOperationException();
	                }
	                @SuppressWarnings("unchecked")
	                @Override
	                public T read(JsonReader in) throws IOException {
	                    return in.peek() == JsonToken.STRING
	                        ? (T) StringTag.valueOf(in.nextString())
	                        : delegate.read(in);
	                }
	            };
			}
			if(type.getRawType() == ByteTag.class){
	            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
	            return new TypeAdapter<T>() {
	                @Override
	                public void write(JsonWriter out, T value) throws IOException {
	                    throw new UnsupportedOperationException();
	                }
	                @SuppressWarnings("unchecked")
	                @Override
	                public T read(JsonReader in) throws IOException {
	                    return in.peek() == JsonToken.NUMBER
	                        ? (T) ByteTag.valueOf((byte)in.nextInt())
	                        : delegate.read(in);
	                }
	            };
			}
			if(type.getRawType() == DoubleTag.class){
	            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
	            return new TypeAdapter<T>() {
	                @Override
	                public void write(JsonWriter out, T value) throws IOException {
	                    throw new UnsupportedOperationException();
	                }
	                @SuppressWarnings("unchecked")
	                @Override
	                public T read(JsonReader in) throws IOException {
	                    return in.peek() == JsonToken.NUMBER
	                        ? (T) DoubleTag.valueOf(in.nextDouble())
	                        : delegate.read(in);
	                }
	            };
			}
			if(type.getRawType() == FloatTag.class){
	            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
	            return new TypeAdapter<T>() {
	                @Override
	                public void write(JsonWriter out, T value) throws IOException {
	                    throw new UnsupportedOperationException();
	                }
	                @SuppressWarnings("unchecked")
	                @Override
	                public T read(JsonReader in) throws IOException {
	                    return in.peek() == JsonToken.NUMBER
	                        ? (T) FloatTag.valueOf((float)in.nextDouble())
	                        : delegate.read(in);
	                }
	            };
			}
			if(type.getRawType() == IntTag.class){
	            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
	            return new TypeAdapter<T>() {
	                @Override
	                public void write(JsonWriter out, T value) throws IOException {
	                    throw new UnsupportedOperationException();
	                }
	                @SuppressWarnings("unchecked")
	                @Override
	                public T read(JsonReader in) throws IOException {
	                    return in.peek() == JsonToken.NUMBER
	                        ? (T) IntTag.valueOf(in.nextInt())
	                        : delegate.read(in);
	                }
	            };
			}
			if(type.getRawType() == LongTag.class){
	            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
	            return new TypeAdapter<T>() {
	                @Override
	                public void write(JsonWriter out, T value) throws IOException {
	                    throw new UnsupportedOperationException();
	                }
	                @SuppressWarnings("unchecked")
	                @Override
	                public T read(JsonReader in) throws IOException {
	                    return in.peek() == JsonToken.NUMBER
	                        ? (T) LongTag.valueOf(in.nextLong())
	                        : delegate.read(in);
	                }
	            };
			}
			if(type.getRawType() == ShortTag.class){
	            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
	            return new TypeAdapter<T>() {
	                @Override
	                public void write(JsonWriter out, T value) throws IOException {
	                    throw new UnsupportedOperationException();
	                }
	                @SuppressWarnings("unchecked")
	                @Override
	                public T read(JsonReader in) throws IOException {
	                    return in.peek() == JsonToken.NUMBER
	                        ? (T) ShortTag.valueOf((short)in.nextInt())
	                        : delegate.read(in);
	                }
	            };
			}

			return null;
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
            public <N extends Tag> NbtRef<N> create(N value) {
                return new NbtRef<>(this, null, value);
            }
        };

        public NbtRef<?> create(NbtPath path) {
            throw new UnsupportedOperationException();
        }

        public <N extends Tag> NbtRef<N> create(N value) {
            throw new UnsupportedOperationException();
        }
    }
}
