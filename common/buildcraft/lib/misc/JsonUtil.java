/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.StreamSupport;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

import net.minecraftforge.common.util.Constants;
import net.minecraft.fluid.Fluid;
import buildcraft.lib.compat.FluidRegistryBC;
import buildcraft.lib.compat.FluidStackBC;
// STUB: ForgeRegistries removed

import buildcraft.api.core.BCLog;

import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class JsonUtil {

    public static final JsonDeserializer<FluidStackBC> FLUID_STACK_DESERIALIZER = (json, type, ctx) -> {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String name = json.getAsString();
            Fluid fluid = FluidRegistryBC.getFluid(name);
            if (fluid == null) {
                throw failAndListFluids(name);
            } else {
                return new FluidStackBC(fluid, 1);
            }
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            String id = obj.get("id").getAsString();
            Fluid fluid = FluidRegistryBC.getFluid(id);
            if (fluid == null) {
                throw failAndListFluids(id);
            }
            int amount = 1;
            if (obj.has("amount")) {
                amount = obj.get("amount").getAsInt();
            }
            // TODO: NBT
            return new FluidStackBC(fluid, amount);
        } else {
            throw new JsonSyntaxException("Expected either a string or an object, got " + json);
        }
    };

    private static JsonSyntaxException failAndListFluids(String name) {
        Set<String> knownFluids = FluidRegistryBC.getRegisteredFluids().keySet();
        String msg = "Unknown fluid '" + name + "'.";
        msg += "\nKnown types:";
        for (String known : new TreeSet<>(knownFluids)) {
            msg += "\n   " + known;
        }
        throw new JsonSyntaxException(msg);
    }

    public static final JsonDeserializer<ItemStack> ITEM_STACK_DESERIALIZER = (json, type, ctx) -> {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String name = json.getAsString();
            Identifier id = new Identifier(name);
            {
                Item foundItem = net.minecraft.registry.Registries.ITEM.get(id);
                if (foundItem == null) throw new com.google.gson.JsonSyntaxException("Unknown item: " + id);
                return new net.minecraft.item.ItemStack(foundItem);
            }
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            String id = obj.get("id").getAsString();
            Identifier loc = new Identifier(id);
            if (!(net.minecraft.registry.Registries.ITEM.get(loc) != null)) {
                throw new JsonSyntaxException("Unknown item '" + id + "'");
            }
            Item item = net.minecraft.registry.Registries.ITEM.get(loc);
            int count = 1;
            if (obj.has("count")) {
                count = JsonUtil.getInt(obj, "count");
            }
            int meta = 0;
            if (obj.has("data")) {
                meta = JsonUtil.getInt(obj, "data");
            } else if (obj.has("meta")) {
                BCLog.logger.warn("[lib.recipe] Found deprecated item 'meta' tag inside of " + json);
                meta = JsonUtil.getInt(obj, "meta");
            }
            // TODO: NBT!
            return new ItemStack(item, count);
        } else {
            throw new JsonSyntaxException("Expected either a string or an object, got " + json);
        }
    };

    public static <K, V> ImmutableMap<K, V> getSubAsImmutableMap(JsonObject obj, String sub,
        TypeToken<HashMap<K, V>> token) {
        if (!obj.has(sub)) {
            return ImmutableMap.of();
        }
        try {
            JsonElement elem = obj.get(sub);
            HashMap<K, V> map = new Gson().fromJson(elem, token.getType());
            return ImmutableMap.copyOf(map);

        } catch (IllegalStateException ise) {
            throw new JsonSyntaxException("Something was wrong with " + obj + " when deserializing it as a " + token,
                ise);
        }
    }

    public static <T> ImmutableList<T> getSubAsImmutableList(JsonObject obj, String sub,
        TypeToken<ArrayList<T>> token) {
        if (!obj.has(sub)) {
            return ImmutableList.of();
        }
        try {
            JsonElement elem = obj.get(sub);
            ArrayList<T> list = new Gson().fromJson(elem, token.getType());
            return ImmutableList.copyOf(list);
        } catch (IllegalStateException ise) {
            throw new JsonSyntaxException("Something was wrong with " + obj + " when deserializing it as a " + token,
                ise);
        }
    }

    public static float getAsFloat(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new JsonSyntaxException("Needed a primitive, but got " + element);
        }
        JsonPrimitive prim = element.getAsJsonPrimitive();
        try {
            return prim.getAsFloat();
        } catch (NumberFormatException nfe) {
            throw new JsonSyntaxException("Expected a valid float, but got " + prim, nfe);
        }
    }

    public static float[] getAsFloatArray(JsonElement elem) {
        if (elem.isJsonArray()) {
            JsonArray array = elem.getAsJsonArray();
            float[] floats = new float[array.size()];
            for (int i = 0; i < floats.length; i++) {
                floats[i] = getAsFloat(array.get(i));
            }
            return floats;
        } else if (elem.isJsonPrimitive()) {
            return new float[] { getAsFloat(elem) };
        } else {
            throw new JsonSyntaxException("Needed an array of floats or a single float but got " + elem);
        }
    }

    public static float[] getSubAsFloatArray(JsonObject obj, String string) {
        if (!obj.has(string)) {
            throw new JsonSyntaxException("Required member " + string + " in " + obj);
        }
        return getAsFloatArray(obj.get(string));
    }

    public static String getAsString(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new JsonSyntaxException("Needed a primitive, but got " + element);
        }
        return element.getAsString();
    }

    public static String[] getAsStringArray(JsonElement elem) {
        if (elem.isJsonArray()) {
            JsonArray array = elem.getAsJsonArray();
            String[] strings = new String[array.size()];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = getAsString(array.get(i));
            }
            return strings;
        } else if (elem.isJsonPrimitive()) {
            return new String[] { getAsString(elem) };
        } else {
            throw new JsonSyntaxException("Needed an array of strings or a single string but got " + elem);
        }
    }

    public static String[] getSubAsStringArray(JsonObject obj, String string) {
        if (!obj.has(string)) {
            throw new JsonSyntaxException("Required member " + string + " in " + obj);
        }
        return getAsStringArray(obj.get(string));
    }

    /** Tries to get a translatable text component from the json as a string. This will either get the prefix directly
     * for a {@link TranslatableText}, or the prefix plus "_raw" for a raw {@link LiteralText}. */
    public static Text getTextComponent(JsonObject json, String subPrefix, String localePrefix) {
        if (json.has(subPrefix)) {
            String str = json.get(subPrefix).getAsString();
            Object[] args;
            if (json.has(subPrefix + "_args")) {
                args = getSubAsStringArray(json, subPrefix + "_args");
            } else {
                args = new String[0];
            }
            return Text.translatable(localePrefix + str, args);
        } else if (json.has(subPrefix + "_raw")) {
            return Text.literal(json.get(subPrefix + "_raw").getAsString());
        } else {
            throw new JsonSyntaxException(
                "Expected to find either '" + subPrefix + "' or '" + subPrefix + "_raw', but got neither for " + json);
        }
    }

    public static Identifier getIdentifier(JsonObject obj, String sub) {
        Identifier ident = getIdentifier(obj, sub, null);
        if (ident == null) {
            throw new JsonSyntaxException("Expected to find '" + sub + "' as a string, but found nothing!");
        }
        return ident;
    }

    public static Identifier getIdentifier(JsonObject obj, String sub, Identifier _default) {
        if (!obj.has(sub)) {
            return _default;
        }
        String str = obj.get(sub).getAsString().toLowerCase(Locale.ROOT);
        int index = str.indexOf(':');
        if (index < 0) {
            throw new JsonSyntaxException("Expected 'domain:path', but didn't find a colon!");
        }
        String domain = str.substring(0, index);
        String path = str.substring(index + 1);
        return new Identifier(domain, path);
    }

    public static int getInt(JsonObject obj, String string) {
        if (obj.has(string)) {
            return getAsInt(obj.get(string));
        }
        throw new JsonSyntaxException("Expected a value for '" + string + "', but found nothing!");
    }

    public static int getAsInt(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new JsonSyntaxException("Needed a primitive, but got " + element);
        }
        JsonPrimitive prim = element.getAsJsonPrimitive();
        if (prim.isNumber()) {
            return prim.getAsInt();
        }
        if (prim.isString()) {
            try {
                INodeLong exp = GenericExpressionCompiler.compileExpressionLong(prim.getAsString());
                return (int) exp.evaluate();
            } catch (InvalidExpressionException iee) {
                throw new JsonSyntaxException("Expected an int or an expression, but got '" + prim + "'", iee);
            }
        }
        throw new JsonSyntaxException("Needed a primitive, but got " + element);
    }

    public static int[] getAsIntArray(JsonElement elem) {
        if (elem.isJsonArray()) {
            JsonArray array = elem.getAsJsonArray();
            int[] strings = new int[array.size()];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = getAsInt(array.get(i));
            }
            return strings;
        } else if (elem.isJsonPrimitive()) {
            return new int[] { getAsInt(elem) };
        } else {
            throw new JsonSyntaxException("Needed an array of ints or a single int but got " + elem);
        }
    }

    public static int[] getSubAsIntArray(JsonObject obj, String string) {
        if (!obj.has(string)) {
            throw new JsonSyntaxException("Required member " + string + " in " + obj);
        }
        return getAsIntArray(obj.get(string));
    }

    public static Map<String, String> deserializeStringMap(JsonObject obj, String sub) {
        JsonElement element = obj.get(sub);
        if (element == null) {
            throw new JsonSyntaxException("Expected to have the element '" + sub + "' inside of '" + obj + "'");
        }
        if (!element.isJsonObject()) {
            throw new JsonSyntaxException("Expected to find an object, but got '" + element + "'");
        }
        return deserializeStringMap(element.getAsJsonObject());
    }

    public static Map<String, String> deserializeStringMap(JsonObject obj) {
        Map<String, String> map = new LinkedHashMap<>();
        for (Entry<String, JsonElement> key : obj.entrySet()) {
            JsonElement value = key.getValue();
            if (value.isJsonPrimitive()) {
                map.put(key.getKey(), value.getAsString());
            } else {
                throw new JsonSyntaxException("Expected a string, but got '" + value + "'");
            }
        }
        return map;
    }

    public static JsonObject inlineCustom(JsonObject obj) {
        if (obj.has("inlines")) {
            JsonElement inlineElems = obj.get("inlines");
            if (!inlineElems.isJsonObject()) {
                throw new JsonSyntaxException("Expected an object, but got '" + inlineElems + "'");
            }
            JsonObject inlines = inlineElems.getAsJsonObject();
            Map<String, JsonObject> inlineMap = new HashMap<>();
            for (Entry<String, JsonElement> entry : inlines.entrySet()) {
                JsonElement elem = entry.getValue();
                if (!elem.isJsonObject()) {
                    throw new JsonSyntaxException("Expected an object, but got '" + elem + "'");
                }
                inlineMap.put(entry.getKey(), elem.getAsJsonObject());
            }
            obj.remove("inlines");
            inline(obj, inlineMap);
        }
        return obj;
    }

    private static void inline(JsonElement element, Map<String, JsonObject> inlineMap) {
        if (element instanceof JsonObject) {
            inline((JsonObject) element, inlineMap);
        } else if (element instanceof JsonArray) {
            JsonArray arr = (JsonArray) element;
            for (JsonElement elem : arr) {
                inline(elem, inlineMap);
            }
        }
    }

    private static void inline(JsonObject obj, Map<String, JsonObject> inlineMap) {
        if (obj.has("inline")) {
            JsonElement in = obj.remove("inline");
            if (!in.isJsonPrimitive() || !in.getAsJsonPrimitive().isString()) {
                throw new JsonSyntaxException("Expected a string, but got '" + in + "'");
            }
            String target = in.getAsString();
            JsonObject toInline = inlineMap.get(target);
            if (toInline == null) {
                throw new JsonSyntaxException("Didn't find the inline " + target);
            }
            for (Entry<String, JsonElement> entry : toInline.entrySet()) {
                String name = entry.getKey();
                if ("inline".equals(name)) {
                    continue;
                }
                if (!obj.has(name)) {
                    /* FIXME: We really need to deep-copy the element, as then we protect against removing an element
                     * from it and ruining it for everyone. */
                    obj.add(name, entry.getValue());
                }
            }
        }
        for (Entry<String, JsonElement> entry : obj.entrySet()) {
            inline(entry.getValue(), inlineMap);
        }
    }

    public static void registerTypeAdaptors(GsonBuilder builder) {
        builder.registerTypeAdapter(FluidStackBC.class, FLUID_STACK_DESERIALIZER);
        builder.registerTypeAdapter(ItemStack.class, ITEM_STACK_DESERIALIZER);
        // TODO: Ingredient deserialiser!
        registerNbtSerializersDeserializers(builder);
    }

    public static GsonBuilder registerNbtSerializersDeserializers(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return type.getRawType() == NbtElement.class ? new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        // noinspection unchecked, RedundantCast
                        Streams.write(((JsonSerializer<T>) (JsonSerializer<NbtElement>) (src, typeOfSrc, context) -> {
                            if (src == NBTUtilBC.NBT_NULL) {
                                return JsonNull.INSTANCE;
                            }
                            switch (src.getType()) {
                                case NbtElement.BYTE_TYPE:
                                    return context.serialize(src, NbtByte.class);
                                case NbtElement.SHORT_TYPE:
                                    return context.serialize(src, NbtShort.class);
                                case NbtElement.INT_TYPE:
                                    return context.serialize(src, NbtInt.class);
                                case NbtElement.LONG_TYPE:
                                    return context.serialize(src, NbtLong.class);
                                case NbtElement.FLOAT_TYPE:
                                    return context.serialize(src, NbtFloat.class);
                                case NbtElement.DOUBLE_TYPE:
                                    return context.serialize(src, NbtDouble.class);
                                case NbtElement.BYTE_ARRAY_TYPE:
                                    return context.serialize(src, NbtByteArray.class);
                                case NbtElement.STRING_TYPE:
                                    return context.serialize(src, NbtString.class);
                                case NbtElement.LIST_TYPE:
                                    return context.serialize(src, NbtList.class);
                                case NbtElement.COMPOUND_TYPE:
                                    return context.serialize(src, NbtCompound.class);
                                case NbtElement.INT_ARRAY_TYPE:
                                    return context.serialize(src, NbtIntArray.class);
                                default:
                                    throw new IllegalArgumentException(src.toString());
                            }
                        }).serialize(value, type.getType(), new JsonSerializationContext() {
                            @Override
                            public JsonElement serialize(Object src) {
                                return gson.toJsonTree(src);
                            }

                            @Override
                            public JsonElement serialize(Object src, Type typeOfSrc) {
                                return gson.toJsonTree(src, typeOfSrc);
                            }
                        }), out);
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        return ((JsonDeserializer<T>) (json, typeOfT, context) -> {
                            if (json.isJsonNull()) {
                                // noinspection unchecked
                                return (T) NBTUtilBC.NBT_NULL;
                            }
                            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                                Number number = json.getAsJsonPrimitive().getAsNumber();
                                if (number instanceof BigInteger || number instanceof Long || number instanceof Integer
                                    || number instanceof Short || number instanceof Byte) {
                                    return context.deserialize(json, NbtLong.class);
                                } else {
                                    return context.deserialize(json, NbtDouble.class);
                                }
                            }
                            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
                                return context.deserialize(
                                    new JsonPrimitive(json.getAsJsonPrimitive().getAsBoolean() ? (byte) 1 : (byte) 0),
                                    NbtByte.class);
                            }
                            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                                return context.deserialize(json, NbtString.class);
                            }
                            if (json.isJsonArray()) {
                                return context.deserialize(json, NbtList.class);
                            }
                            if (json.isJsonObject()) {
                                return context.deserialize(json, NbtCompound.class);
                            }
                            throw new IllegalArgumentException(json.toString());
                        }).deserialize(Streams.parse(in), type.getType(), gson::fromJson);
                    }
                } : null;
            }
        }).registerTypeAdapter(NbtByte.class,
            (JsonSerializer<NbtByte>) (src, typeOfSrc, context) -> new JsonPrimitive(src.byteValue()))
            .registerTypeAdapter(NbtByte.class,
                (JsonDeserializer<
                    NbtByte>) (json, typeOfT, context) -> NbtByte.of(json.getAsJsonPrimitive().getAsByte()))
            .registerTypeAdapter(NbtShort.class,
                (JsonSerializer<NbtShort>) (src, typeOfSrc, context) -> new JsonPrimitive(src.shortValue()))
            .registerTypeAdapter(NbtShort.class,
                (JsonDeserializer<
                    NbtShort>) (json, typeOfT, context) -> NbtShort.of(json.getAsJsonPrimitive().getAsShort()))
            .registerTypeAdapter(NbtInt.class,
                (JsonSerializer<NbtInt>) (src, typeOfSrc, context) -> new JsonPrimitive(src.intValue()))
            .registerTypeAdapter(NbtInt.class,
                (JsonDeserializer<
                    NbtInt>) (json, typeOfT, context) -> NbtInt.of(json.getAsJsonPrimitive().getAsInt()))
            .registerTypeAdapter(NbtLong.class,
                (JsonSerializer<NbtLong>) (src, typeOfSrc, context) -> new JsonPrimitive(src.longValue()))
            .registerTypeAdapter(NbtLong.class,
                (JsonDeserializer<
                    NbtLong>) (json, typeOfT, context) -> NbtLong.of(json.getAsJsonPrimitive().getAsLong()))
            .registerTypeAdapter(NbtFloat.class,
                (JsonSerializer<NbtFloat>) (src, typeOfSrc, context) -> new JsonPrimitive(src.floatValue()))
            .registerTypeAdapter(NbtFloat.class,
                (JsonDeserializer<
                    NbtFloat>) (json, typeOfT, context) -> NbtFloat.of(json.getAsJsonPrimitive().getAsFloat()))
            .registerTypeAdapter(NbtDouble.class,
                (JsonSerializer<NbtDouble>) (src, typeOfSrc, context) -> new JsonPrimitive(src.doubleValue()))
            .registerTypeAdapter(NbtDouble.class,
                (JsonDeserializer<NbtDouble>) (json, typeOfT,
                    context) -> NbtDouble.of(json.getAsJsonPrimitive().getAsDouble()))
            .registerTypeAdapter(NbtByteArray.class, (JsonSerializer<NbtByteArray>) (src, typeOfSrc, context) -> {
                JsonArray jsonArray = new JsonArray();
                for (byte element : src.getByteArray()) {
                    jsonArray.add(new JsonPrimitive(element));
                }
                return jsonArray;
            })
            .registerTypeAdapter(NbtByteArray.class,
                (JsonDeserializer<NbtByteArray>) (json, typeOfT, context) -> new NbtByteArray(
                    ArrayUtils.toPrimitive(StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                        .map(JsonElement::getAsByte).toArray(Byte[]::new))))
            .registerTypeAdapter(NbtString.class,
                (JsonSerializer<NbtString>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getString()))
            .registerTypeAdapter(NbtString.class,
                (JsonDeserializer<NbtString>) (json, typeOfT,
                    context) -> NbtString.of(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(NbtList.class, (JsonSerializer<NbtList>) (src, typeOfSrc, context) -> {
                JsonArray jsonArray = new JsonArray();
                for (int i = 0; i < src.size(); i++) {
                    NbtElement element = src.get(i);
                    jsonArray.add(context.serialize(element, NbtElement.class));
                }
                return jsonArray;
            }).registerTypeAdapter(NbtList.class, (JsonDeserializer<NbtList>) (json, typeOfT, context) -> {
                NbtList nbtTagList = new NbtList();
                StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                    .map(element -> context.<NbtElement> deserialize(element, NbtElement.class))
                    .forEach(nbtTagList::appendTag);
                return nbtTagList;
            }).registerTypeAdapter(NbtCompound.class, (JsonSerializer<NbtCompound>) (src, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();
                for (String key : src.getKeys()) {
                    jsonObject.add(key, context.serialize(src.get(key), NbtElement.class));
                }
                return jsonObject;
            })
            .registerTypeAdapter(NbtCompound.class, (JsonDeserializer<NbtCompound>) (json, typeOfT, context) -> {
                NbtCompound nbtTagCompound = new NbtCompound();
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                    nbtTagCompound.put(entry.getKey(), context.deserialize(entry.getValue(), NbtElement.class));
                }
                return nbtTagCompound;
            }).registerTypeAdapter(NbtIntArray.class, (JsonSerializer<NbtIntArray>) (src, typeOfSrc, context) -> {
                JsonArray jsonArray = new JsonArray();
                for (int element : src.getIntArray()) {
                    jsonArray.add(new JsonPrimitive(element));
                }
                return jsonArray;
            }).registerTypeAdapter(NbtIntArray.class,
                (JsonDeserializer<NbtIntArray>) (json, typeOfT, context) -> new NbtIntArray(StreamSupport
                    .stream(json.getAsJsonArray().spliterator(), false).mapToInt(JsonElement::getAsByte).toArray()));
    }

    public static JsonObject inheritTags(JsonObject parent, JsonObject overwrite) {
        JsonObject object = new JsonObject();

        for (Entry<String, JsonElement> entry : overwrite.entrySet()) {
            String key = entry.getKey();
            JsonElement element = entry.getValue();
            JsonElement alternate = parent.get(key);
            if (element instanceof JsonObject && alternate instanceof JsonObject) {
                object.add(key, inheritTags(alternate.getAsJsonObject(), element.getAsJsonObject()));
                // } else if (element instanceof JsonArray && alternate instanceof JsonArray) {
            } else {
                object.add(key, element);
            }
        }
        for (Entry<String, JsonElement> entry : parent.entrySet()) {
            String key = entry.getKey();
            JsonElement element = entry.getValue();
            if (!object.has(key)) {
                object.add(key, element);
            }
        }
        return object;
    }
}
