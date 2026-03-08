/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.misc.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import net.minecraft.nbt.INBT;

import java.util.Arrays;
import java.util.Objects;

public enum EnumNbtCompareOperation {
    EQ("=") {
        @Override
        public boolean compare(INBT a, INBT b) {
            return Objects.equals(GSON.toJson(a, INBT.class), GSON.toJson(b, INBT.class));
        }
    },
    NQE("!=") {
        @Override
        public boolean compare(INBT a, INBT b) {
            return !EQ.compare(a, b);
        }
    };

    private static final Gson GSON = JsonUtil.registerNbtSerializersDeserializers(new GsonBuilder()).create();

    public final String name;

    EnumNbtCompareOperation(String name) {
        this.name = name;
    }

    public static EnumNbtCompareOperation byName(String name) {
        return Arrays.stream(values())
                .filter(type -> type.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Compare operation not found"));
    }

    public abstract boolean compare(INBT a, INBT b);

    public static final JsonDeserializer<EnumNbtCompareOperation> DESERIALIZER = (json, typeOfT, context) ->
            byName(json.getAsJsonPrimitive().getAsString());
}
