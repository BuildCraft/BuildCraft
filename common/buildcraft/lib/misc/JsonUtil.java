/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.StreamSupport;

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
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import net.minecraftforge.common.util.Constants;

public class JsonUtil {
    public static <K, V> ImmutableMap<K, V> getSubAsImmutableMap(JsonObject obj, String sub, TypeToken<HashMap<K, V>> token) {
        if (!obj.has(sub)) {
            return ImmutableMap.of();
        }
        try {
            JsonElement elem = obj.get(sub);
            HashMap<K, V> map = new Gson().fromJson(elem, token.getType());
            return ImmutableMap.copyOf(map);

        } catch (IllegalStateException ise) {
            throw new JsonSyntaxException("Something was wrong with " + obj + " when deserializing it as a " + token, ise);
        }
    }

    public static <T> ImmutableList<T> getSubAsImmutableList(JsonObject obj, String sub, TypeToken<ArrayList<T>> token) {
        if (!obj.has(sub)) {
            return ImmutableList.of();
        }
        try {
            JsonElement elem = obj.get(sub);
            ArrayList<T> list = new Gson().fromJson(elem, token.getType());
            return ImmutableList.copyOf(list);
        } catch (IllegalStateException ise) {
            throw new JsonSyntaxException("Something was wrong with " + obj + " when deserializing it as a " + token, ise);
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
        Map<String, String> map = new HashMap<>();
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
                if ("inline".equals(name)) continue;
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

    public static GsonBuilder registerNbtSerializersDeserializers(GsonBuilder gsonBuilder) {
        return gsonBuilder
            .registerTypeAdapter(
                NBTBase.class,
                (JsonSerializer<NBTBase>) (src, typeOfSrc, context) -> {
                    if (src == null) {
                        return JsonNull.INSTANCE;
                    }
                    switch (src.getId()) {
                        case Constants.NBT.TAG_BYTE:
                            return context.serialize(src, NBTTagByte.class);
                        case Constants.NBT.TAG_SHORT:
                            return context.serialize(src, NBTTagShort.class);
                        case Constants.NBT.TAG_INT:
                            return context.serialize(src, NBTTagInt.class);
                        case Constants.NBT.TAG_LONG:
                            return context.serialize(src, NBTTagLong.class);
                        case Constants.NBT.TAG_FLOAT:
                            return context.serialize(src, NBTTagFloat.class);
                        case Constants.NBT.TAG_DOUBLE:
                            return context.serialize(src, NBTTagDouble.class);
                        case Constants.NBT.TAG_BYTE_ARRAY:
                            return context.serialize(src, NBTTagByteArray.class);
                        case Constants.NBT.TAG_STRING:
                            return context.serialize(src, NBTTagString.class);
                        case Constants.NBT.TAG_LIST:
                            return context.serialize(src, NBTTagList.class);
                        case Constants.NBT.TAG_COMPOUND:
                            return context.serialize(src, NBTTagCompound.class);
                        case Constants.NBT.TAG_INT_ARRAY:
                            return context.serialize(src, NBTTagIntArray.class);
                        default:
                            throw new IllegalArgumentException(src.toString());
                    }
                }
            )
            .registerTypeAdapter(
                NBTBase.class,
                (JsonDeserializer<NBTBase>) (json, typeOfT, context) -> {
                    if (json.isJsonNull()) {
                        return null;
                    }
                    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                        Number number = json.getAsJsonPrimitive().getAsNumber();
                        if (number instanceof BigInteger ||
                            number instanceof Long ||
                            number instanceof Integer ||
                            number instanceof Short ||
                            number instanceof Byte) {
                            return context.deserialize(json, NBTTagLong.class);
                        } else {
                            return context.deserialize(json, NBTTagDouble.class);
                        }
                    }
                    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
                        return context.deserialize(
                            new JsonPrimitive(
                                json.getAsJsonPrimitive().getAsBoolean()
                                    ? (byte) 1
                                    : (byte) 0
                            ),
                            NBTTagByte.class
                        );
                    }
                    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                        return context.deserialize(json, NBTTagString.class);
                    }
                    if (json.isJsonArray()) {
                        return context.deserialize(json, NBTTagList.class);
                    }
                    if (json.isJsonObject()) {
                        return context.deserialize(json, NBTTagCompound.class);
                    }
                    throw new IllegalArgumentException(json.toString());
                }
            )
            .registerTypeAdapter(
                NBTTagByte.class,
                (JsonSerializer<NBTTagByte>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.getByte())
            )
            .registerTypeAdapter(
                NBTTagByte.class,
                (JsonDeserializer<NBTTagByte>) (json, typeOfT, context) ->
                    new NBTTagByte(json.getAsJsonPrimitive().getAsByte())
            )
            .registerTypeAdapter(
                NBTTagShort.class,
                (JsonSerializer<NBTTagShort>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.getShort())
            )
            .registerTypeAdapter(
                NBTTagShort.class,
                (JsonDeserializer<NBTTagShort>) (json, typeOfT, context) ->
                    new NBTTagShort(json.getAsJsonPrimitive().getAsShort())
            )
            .registerTypeAdapter(
                NBTTagInt.class,
                (JsonSerializer<NBTTagInt>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.getInt())
            )
            .registerTypeAdapter(
                NBTTagInt.class,
                (JsonDeserializer<NBTTagInt>) (json, typeOfT, context) ->
                    new NBTTagInt(json.getAsJsonPrimitive().getAsInt())
            )
            .registerTypeAdapter(
                NBTTagLong.class,
                (JsonSerializer<NBTTagLong>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.getLong())
            )
            .registerTypeAdapter(
                NBTTagLong.class,
                (JsonDeserializer<NBTTagLong>) (json, typeOfT, context) ->
                    new NBTTagLong(json.getAsJsonPrimitive().getAsLong())
            )
            .registerTypeAdapter(
                NBTTagFloat.class,
                (JsonSerializer<NBTTagFloat>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.getFloat())
            )
            .registerTypeAdapter(
                NBTTagFloat.class,
                (JsonDeserializer<NBTTagFloat>) (json, typeOfT, context) ->
                    new NBTTagFloat(json.getAsJsonPrimitive().getAsFloat())
            )
            .registerTypeAdapter(
                NBTTagDouble.class,
                (JsonSerializer<NBTTagDouble>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.getDouble())
            )
            .registerTypeAdapter(
                NBTTagDouble.class,
                (JsonDeserializer<NBTTagDouble>) (json, typeOfT, context) ->
                    new NBTTagDouble(json.getAsJsonPrimitive().getAsDouble())
            )
            .registerTypeAdapter(
                NBTTagByteArray.class,
                (JsonSerializer<NBTTagByteArray>) (src, typeOfSrc, context) -> {
                    JsonArray jsonArray = new JsonArray();
                    for (byte element : src.getByteArray()) {
                        jsonArray.add(new JsonPrimitive(element));
                    }
                    return jsonArray;
                }
            )
            .registerTypeAdapter(
                NBTTagByteArray.class,
                (JsonDeserializer<NBTTagByteArray>) (json, typeOfT, context) ->
                    new NBTTagByteArray(
                        ArrayUtils.toPrimitive(
                            StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                                .map(JsonElement::getAsByte)
                                .toArray(Byte[]::new)
                        )
                    )
            )
            .registerTypeAdapter(
                NBTTagString.class,
                (JsonSerializer<NBTTagString>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.getString())
            )
            .registerTypeAdapter(
                NBTTagString.class,
                (JsonDeserializer<NBTTagString>) (json, typeOfT, context) ->
                    new NBTTagString(json.getAsJsonPrimitive().getAsString())
            )
            .registerTypeAdapter(
                NBTTagList.class,
                (JsonSerializer<NBTTagList>) (src, typeOfSrc, context) -> {
                    JsonArray jsonArray = new JsonArray();
                    for (int i = 0; i < src.tagCount(); i++) {
                        NBTBase element = src.get(i);
                        jsonArray.add(context.serialize(element, NBTBase.class));
                    }
                    return jsonArray;
                }
            )
            .registerTypeAdapter(
                NBTTagList.class,
                (JsonDeserializer<NBTTagList>) (json, typeOfT, context) -> {
                    NBTTagList nbtTagList = new NBTTagList();
                    StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                        .map(element -> context.<NBTBase>deserialize(element, NBTBase.class))
                        .forEach(nbtTagList::appendTag);
                    return nbtTagList;
                }
            )
            .registerTypeAdapter(
                NBTTagCompound.class,
                (JsonSerializer<NBTTagCompound>) (src, typeOfSrc, context) -> {
                    JsonObject jsonObject = new JsonObject();
                    for (String key : src.getKeySet()) {
                        jsonObject.add(key, context.serialize(src.getTag(key), NBTBase.class));
                    }
                    return jsonObject;
                }
            )
            .registerTypeAdapter(
                NBTTagCompound.class,
                (JsonDeserializer<NBTTagCompound>) (json, typeOfT, context) -> {
                    NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                        nbtTagCompound.setTag(
                            entry.getKey(),
                            Optional.<NBTBase>ofNullable(context.deserialize(entry.getValue(), NBTBase.class))
                                .orElse(NBTUtilBC.NULL_NBT)
                        );
                    }
                    return nbtTagCompound;
                }
            )
            .registerTypeAdapter(
                NBTTagIntArray.class,
                (JsonSerializer<NBTTagIntArray>) (src, typeOfSrc, context) -> {
                    JsonArray jsonArray = new JsonArray();
                    for (int element : src.getIntArray()) {
                        jsonArray.add(new JsonPrimitive(element));
                    }
                    return jsonArray;
                }
            )
            .registerTypeAdapter(
                NBTTagIntArray.class,
                (JsonDeserializer<NBTTagIntArray>) (json, typeOfT, context) ->
                    new NBTTagIntArray(
                        StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                            .mapToInt(JsonElement::getAsByte)
                            .toArray()
                    )
            );
    }
}
