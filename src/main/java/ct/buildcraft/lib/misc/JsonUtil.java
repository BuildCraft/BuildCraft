/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.StreamSupport;

import org.antlr.runtime.misc.IntArray;
import org.apache.commons.lang3.ArrayUtils;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.expression.GenericExpressionCompiler;
import ct.buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import ct.buildcraft.lib.expression.api.InvalidExpressionException;
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
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.Registry;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class JsonUtil {

    public static final JsonDeserializer<FluidStack> FLUID_STACK_DESERIALIZER = (json, type, ctx) -> {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String name = json.getAsString();
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(name));
            if (fluid == null) {
                throw failAndListFluids(name);
            } else {
                return new FluidStack(fluid, 1);
            }
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            String id = obj.get("id").getAsString();
            if(id == null) 
            	BCLog.logger.error("key 'id' is null!");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id));
            if (fluid == null) {
                throw failAndListFluids(id);
            }
            int amount = 1;
            if (obj.has("amount")) {
                amount = obj.get("amount").getAsInt();
            }
            // TODO: NBT
            return new FluidStack(fluid, amount);
        } else {
            throw new JsonSyntaxException("Expected either a string or an object, got " + json);
        }
    };

    private static JsonSyntaxException failAndListFluids(String name) {
        Set<ResourceLocation> knownFluids = ForgeRegistries.FLUIDS.getKeys();
        String msg = "Unknown fluid '" + name + "'.";
        msg += "\nKnown types:";
        for (ResourceLocation known : new TreeSet<>(knownFluids)) 
            msg += "\n   " + known;
        throw new JsonSyntaxException(msg);
    }

    public static final JsonDeserializer<ItemStack> ITEM_STACK_DESERIALIZER = (json, type, ctx) -> {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String name = json.getAsString();
            ResourceLocation id = new ResourceLocation(name);
            if (!ForgeRegistries.ITEMS.containsKey(id)) {
                throw new JsonSyntaxException("Unknown item '" + name + "'");
            } else {
                return new ItemStack(ForgeRegistries.ITEMS.getValue(id));
            }
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            String id = obj.get("id").getAsString();
            ResourceLocation loc = new ResourceLocation(id);
            if (!ForgeRegistries.ITEMS.containsKey(loc)) {
                throw new JsonSyntaxException("Unknown item '" + id + "'");
            }
            Item item = ForgeRegistries.ITEMS.getValue(loc);
            int count = 1;
            if (obj.has("count")) {
                count = JsonUtil.getInt(obj, "count");
            }
            CompoundTag data = null ;
            if (obj.has("data")) {
                data = JsonUtils.readNBT(obj, "data");
            } else if (obj.has("meta")) {
                BCLog.logger.warn("[lib.recipe] Found deprecated item 'meta' tag inside of " + json);
//                meta = JsonUtil.getInt(obj, "meta");
            }
            ItemStack stack = new ItemStack(item, count);
            if(data != null) 
            	stack.setTag(data);
             return stack;
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
     * for a {@link TranslatableContents}, or the prefix plus "_raw" for a raw {@link LiteralContents}. */
    public static Component getTextComponent(JsonObject json, String subPrefix, String localePrefix) {
        if (json.has(subPrefix)) {
        	
            String str = json.get(subPrefix).getAsString();
            Object[] args;
            if (json.has(subPrefix + "_args")) {
                args = getSubAsStringArray(json, subPrefix + "_args");
            } else {
                args = new String[0];
            }
            return Component.translatable(localePrefix + str, args);
        } else if (json.has(subPrefix + "_raw")) {
            return Component.literal(json.get(subPrefix + "_raw").getAsString());
        } else {
            throw new JsonSyntaxException(
                "Expected to find either '" + subPrefix + "' or '" + subPrefix + "_raw', but got neither for " + json);
        }
    }

    public static ResourceLocation getIdentifier(JsonObject obj, String sub) {
        ResourceLocation ident = getIdentifier(obj, sub, null);
        if (ident == null) {
            throw new JsonSyntaxException("Expected to find '" + sub + "' as a string, but found nothing!");
        }
        return ident;
    }

    public static ResourceLocation getIdentifier(JsonObject obj, String sub, ResourceLocation _default) {
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
        return new ResourceLocation(domain, path);
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
        builder.registerTypeAdapter(FluidStack.class, FLUID_STACK_DESERIALIZER);
        builder.registerTypeAdapter(ItemStack.class, ITEM_STACK_DESERIALIZER);
        // TODO: Ingredient deserialiser!
        registerNbtSerializersDeserializers(builder);
    }

    public static GsonBuilder registerNbtSerializersDeserializers(GsonBuilder gsonBuilder) {
        return gsonBuilder.registerTypeAdapterFactory(new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return type.getRawType() == Tag.class ? new TypeAdapter<T>() {
                    @SuppressWarnings("unchecked")
					@Override
                    public void write(JsonWriter out, T value) throws IOException {
                        // noinspection unchecked, RedundantCast
                        Streams.write(((JsonSerializer<T>) (JsonSerializer<Tag>) (src, typeOfSrc, context) -> {
                            if (src == NBTUtilBC.NBT_NULL) {
                                return JsonNull.INSTANCE;
                            }
                            switch (src.getId()) {
                                case Tag.TAG_BYTE:
                                    return context.serialize(src, ByteTag.class);
                                case Tag.TAG_SHORT:
                                    return context.serialize(src, ShortTag.class);
                                case Tag.TAG_INT:
                                    return context.serialize(src, IntTag.class);
                                case Tag.TAG_LONG:
                                    return context.serialize(src, LongTag.class);
                                case Tag.TAG_FLOAT:
                                    return context.serialize(src, FloatTag.class);
                                case Tag.TAG_DOUBLE:
                                    return context.serialize(src, DoubleTag.class);
                                case Tag.TAG_BYTE_ARRAY:
                                    return context.serialize(src, ByteArrayTag.class);
                                case Tag.TAG_STRING:
                                    return context.serialize(src, StringTag.class);
                                case Tag.TAG_LIST:
                                    return context.serialize(src, ListTag.class);
                                case Tag.TAG_COMPOUND:
                                    return context.serialize(src, CompoundTag.class);
                                case Tag.TAG_INT_ARRAY:
                                    return context.serialize(src, IntArrayTag.class);
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
                                    return context.deserialize(json, LongTag.class);
                                } else {
                                    return context.deserialize(json, DoubleTag.class);
                                }
                            }
                            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
                                return context.deserialize(
                                    new JsonPrimitive(json.getAsJsonPrimitive().getAsBoolean() ? (byte) 1 : (byte) 0),
                                    ByteTag.class);
                            }
                            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                                return context.deserialize(json, StringTag.class);
                            }
                            if (json.isJsonArray()) {
                                return context.deserialize(json, ListTag.class);
                            }
                            if (json.isJsonObject()) {
                                return context.deserialize(json, CompoundTag.class);
                            }
                            throw new IllegalArgumentException(json.toString());
                        }).deserialize(Streams.parse(in), type.getType(), gson::fromJson);
                    }
                } : null;
            }
        }).registerTypeAdapter(ByteTag.class,
            (JsonSerializer<ByteTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsByte()))
            .registerTypeAdapter(ByteTag.class,
                (JsonDeserializer<
                    ByteTag>) (json, typeOfT, context) -> ByteTag.valueOf(json.getAsJsonPrimitive().getAsByte()))
            .registerTypeAdapter(ShortTag.class,
                (JsonSerializer<ShortTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsShort()))
            .registerTypeAdapter(ShortTag.class,
                (JsonDeserializer<
                    ShortTag>) (json, typeOfT, context) -> ShortTag.valueOf(json.getAsJsonPrimitive().getAsShort()))
            .registerTypeAdapter(IntTag.class,
                (JsonSerializer<IntTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsInt()))
            .registerTypeAdapter(IntTag.class,
                (JsonDeserializer<
                    IntTag>) (json, typeOfT, context) -> IntTag.valueOf(json.getAsJsonPrimitive().getAsInt()))
            .registerTypeAdapter(LongTag.class,
                (JsonSerializer<LongTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsLong()))
            .registerTypeAdapter(LongTag.class,
                (JsonDeserializer<
                    LongTag>) (json, typeOfT, context) -> LongTag.valueOf(json.getAsJsonPrimitive().getAsLong()))
            .registerTypeAdapter(FloatTag.class,
                (JsonSerializer<FloatTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsFloat()))
            .registerTypeAdapter(FloatTag.class,
                (JsonDeserializer<
                    FloatTag>) (json, typeOfT, context) -> FloatTag.valueOf(json.getAsJsonPrimitive().getAsFloat()))
            .registerTypeAdapter(DoubleTag.class,
                (JsonSerializer<DoubleTag>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsDouble()))
            .registerTypeAdapter(DoubleTag.class,
                (JsonDeserializer<DoubleTag>) (json, typeOfT,
                    context) -> DoubleTag.valueOf(json.getAsJsonPrimitive().getAsDouble()))
            .registerTypeAdapter(ByteArrayTag.class, (JsonSerializer<ByteArrayTag>) (src, typeOfSrc, context) -> {
                JsonArray jsonArray = new JsonArray();
                for (byte element : src.getAsByteArray()) {
                    jsonArray.add(new JsonPrimitive(element));
                }
                return jsonArray;
            })
            .registerTypeAdapter(ByteArrayTag.class,
                (JsonDeserializer<ByteArrayTag>) (json, typeOfT, context) -> new ByteArrayTag(
                    ArrayUtils.toPrimitive(StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                        .map(JsonElement::getAsByte).toArray(Byte[]::new))))
            .registerTypeAdapter(StringTag.class,
                (JsonSerializer<String>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(String.class,
                (JsonDeserializer<String>) (json, typeOfT,
                    context) -> new String(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(ListTag.class, (JsonSerializer<ListTag>) (src, typeOfSrc, context) -> {
                JsonArray jsonArray = new JsonArray();
                for (int i = 0; i < src.size(); i++) {
                    Tag element = src.get(i);
                    jsonArray.add(context.serialize(element, Tag.class));
                }
                return jsonArray;
            }).registerTypeAdapter(ListTag.class, (JsonDeserializer<ListTag>) (json, typeOfT, context) -> {
                ListTag nbtTagList = new ListTag();
                int counter = 0;
                for(Tag t : StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                    .map(element -> context.<Tag> deserialize(element, Tag.class)).toList())
                	nbtTagList.addTag(counter++, t);
                return nbtTagList;
            }).registerTypeAdapter(CompoundTag.class, (JsonSerializer<CompoundTag>) (src, typeOfSrc, context) -> {
                JsonObject jsonObject = new JsonObject();
                for (String key : src.getAllKeys()) {
                    jsonObject.add(key, context.serialize(src.get(key), Tag.class));
                }
                return jsonObject;
            })
            .registerTypeAdapter(CompoundTag.class, (JsonDeserializer<CompoundTag>) (json, typeOfT, context) -> {
                CompoundTag nbtTagCompound = new CompoundTag();
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                    nbtTagCompound.put(entry.getKey(), context.deserialize(entry.getValue(), Tag.class));
                }
                return nbtTagCompound;
            }).registerTypeAdapter(IntArrayTag.class, (JsonSerializer<IntArrayTag>) (src, typeOfSrc, context) -> {
                JsonArray jsonArray = new JsonArray();
                for (int element : src.getAsIntArray()) {
                    jsonArray.add(new JsonPrimitive(element));
                }
                return jsonArray;
            }).registerTypeAdapter(IntArrayTag.class,
                (JsonDeserializer<IntArrayTag>) (json, typeOfT, context) -> new IntArrayTag(StreamSupport
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
    //BUG
    public static final Codec<Pair<ParameterPoint, ResourceKey<Biome>>> BIOME_CODEC = Codec.pair(ParameterPoint.CODEC, ResourceKey.codec(Registry.BIOME_REGISTRY));
    public static final Codec<String> PARA_TO_STRING_CODEC = ParameterPoint.CODEC.xmap(a -> a.toString(), null);
    public static final Codec<Map<String, ResourceKey<Biome>>> BIOME_MAP_CODEC = Codec.unboundedMap(Codec.STRING, ResourceKey.codec(Registry.BIOME_REGISTRY));

    public static ParameterPoint stringToParameterPoint(String s) {
    	Pattern tem = Pattern.compile("temperature\\=\\[(.*?)\\]");
    	return null;
//    	return ParameterPoint.
    }
    
    public static JsonElement codeBiomePair(Pair<ParameterPoint, ResourceKey<Biome>> pair) {
    	return BIOME_CODEC.encodeStart(JsonOps.INSTANCE, pair).getOrThrow(false, (s) -> BCLog.logger.debug("BuildCraft:JsonUtil:Can not codec "+ pair.toString() +"\n"
    			+ "with error :" + s + "\n this should not happen."));
    }
    
    public static Pair<ParameterPoint, ResourceKey<Biome>> decodeBiomePair(String debugInfo ,JsonElement json) {
    	return BIOME_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, (s) -> BCLog.logger.debug("BuildCraft:JsonUtil:Can not decodec "+ debugInfo +"\n"
    			+ "with error :" + s + "\n this should not happen."));
    }
}
