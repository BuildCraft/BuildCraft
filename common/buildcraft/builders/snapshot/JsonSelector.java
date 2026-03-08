/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("WeakerAccess")
public class JsonSelector {
    private final String base;
    @SerializedName("nbt")
    private final List<Expression> expressions;

    private JsonSelector(String base, List<Expression> expressions) {
        this.base = base;
        this.expressions = expressions;
    }

    public boolean matches(Predicate<String> basePredicate, CompoundNBT nbt) {
        return basePredicate.test(base) &&
                expressions.stream()
                        .allMatch(expression -> expression.operation.compare(expression.key.get(nbt), expression.value));
    }

    public static final TypeAdapterFactory TYPE_ADAPTER_FACTORY = new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() != JsonSelector.class) {
                return null;
            }
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
                            ? (T) new JsonSelector(in.nextString(), Collections.emptyList())
                            : delegate.read(in);
                }
            };
        }
    };

    private static class Expression {
        public final NbtPath key;
        public final EnumNbtCompareOperation operation;
        public final INBT value;

        public Expression(NbtPath key, EnumNbtCompareOperation operation, INBT value) {
            this.key = key;
            this.operation = operation;
            this.value = value;
        }
    }
}
