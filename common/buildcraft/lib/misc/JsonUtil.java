/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BCLog;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.InvalidExpressionException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.*;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.StreamSupport;

public class JsonUtil {

    public static final JsonDeserializer<FluidStack> FLUID_STACK_DESERIALIZER = (json, type, ctx) ->
    {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            String name = json.getAsString();
            ResourceLocation fluidName = new ResourceLocation(name); // Calen
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName); // Calen
            if (fluid == null) {
                throw failAndListFluids(name);
            } else {
                return new FluidStack(fluid, 1);
            }
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
//            String id = JsonUtils.getString(obj, "id");
            String id = JSONUtils.getAsString(obj, "id");
            ResourceLocation fluidName = new ResourceLocation(id);
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
            if (fluid == null) {
                throw failAndListFluids(id);
            }
            int amount = 1;
            if (obj.has("amount")) {
//                amount = JsonUtils.getInt(obj, "amount");
                amount = JSONUtils.getAsInt(obj, "amount");
            }
            // TODO: NBT
            return new FluidStack(fluid, amount);
        } else {
            throw new JsonSyntaxException("Expected either a string or an object, got " + json);
        }
    };

    private static JsonSyntaxException failAndListFluids(String name) {
        Set<Entry<RegistryKey<Fluid>, Fluid>> knownFluids = ForgeRegistries.FLUIDS.getEntries();
        String msg = "Unknown fluid '" + name + "'.";
        msg += "\nKnown types:";
        for (Entry<RegistryKey<Fluid>, Fluid> known : new TreeSet<>(knownFluids)) {
            msg += "\n   " + known;
        }
        throw new JsonSyntaxException(msg);
    }

    public static final JsonDeserializer<ItemStack> ITEM_STACK_DESERIALIZER = (json, type, ctx) ->
    {
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
//            String id = JsonUtils.getString(obj, "id");
            String id = JSONUtils.getAsString(obj, "id");
            ResourceLocation loc = new ResourceLocation(id);
            if (!ForgeRegistries.ITEMS.containsKey(loc)) {
                throw new JsonSyntaxException("Unknown item '" + id + "'");
            }
            Item item = ForgeRegistries.ITEMS.getValue(loc);
            int count = 1;
            if (obj.has("count")) {
                count = JsonUtil.getInt(obj, "count");
            }
            int meta = 0;
            if (obj.has("data")) {
                meta = JsonUtil.getInt(obj, "data");
            } else if (obj.has("meta")) {
//                BCLog.logger.warn("[lib.recipe] Found deprecated item 'meta' tag inside of " + json);
                BCLog.logger.error("[lib.recipe] Found deprecated item 'meta' tag inside of " + json);
                meta = JsonUtil.getInt(obj, "meta");
            }
            // TODO: NBT!
//            return new ItemStack(item, count, meta);
            return new ItemStack(item, count);
        } else {
            throw new JsonSyntaxException("Expected either a string or an object, got " + json);
        }
    };

    public static <K, V> ImmutableMap<K, V> getSubAsImmutableMap(JsonObject obj, String sub, TypeToken<HashMap<K, V>> token) {
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

    public static <T> ImmutableList<T> getSubAsImmutableList(JsonObject obj, String sub, TypeToken<ArrayList<T>> token) {
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
     * for a {@link TranslationTextComponent}, or the prefix plus "_raw" for a raw {@link StringTextComponent}. */
    public static IFormattableTextComponent getTextComponent(JsonObject json, String subPrefix, String localePrefix) {
        if (json.has(subPrefix)) {
//            String str = JsonUtils.getString(json, subPrefix);
            String str = JSONUtils.getAsString(json, subPrefix);
            Object[] args;
            if (json.has(subPrefix + "_args")) {
                args = getSubAsStringArray(json, subPrefix + "_args");
            } else {
                args = new String[0];
            }
            return new TranslationTextComponent(localePrefix + str, args);
        } else if (json.has(subPrefix + "_raw")) {
//            return new StringTextComponent(JsonUtils.getString(json, subPrefix + "_raw"));
            return new StringTextComponent(JSONUtils.getAsString(json, subPrefix + "_raw"));
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
//        String str = JsonUtils.getString(obj, sub).toLowerCase(Locale.ROOT);
        String str = JSONUtils.getAsString(obj, sub).toLowerCase(Locale.ROOT);
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
                        return type.getRawType() == INBT.class ? new TypeAdapter<T>() {
                            @Override
                            public void write(JsonWriter out, T value) throws IOException {
                                // noinspection unchecked, RedundantCast
                                Streams.write(((JsonSerializer<T>) (JsonSerializer<INBT>) (src, typeOfSrc, context) ->
                                {
                                    if (src == NBTUtilBC.NBT_NULL) {
                                        return JsonNull.INSTANCE;
                                    }
                                    switch (src.getId()) {
                                        case Constants.NBT.TAG_BYTE:
                                            return context.serialize(src, ByteNBT.class);
                                        case Constants.NBT.TAG_SHORT:
                                            return context.serialize(src, ShortNBT.class);
                                        case Constants.NBT.TAG_INT:
                                            return context.serialize(src, IntNBT.class);
                                        case Constants.NBT.TAG_LONG:
                                            return context.serialize(src, LongNBT.class);
                                        case Constants.NBT.TAG_FLOAT:
                                            return context.serialize(src, FloatNBT.class);
                                        case Constants.NBT.TAG_DOUBLE:
                                            return context.serialize(src, DoubleNBT.class);
                                        case Constants.NBT.TAG_BYTE_ARRAY:
                                            return context.serialize(src, ByteArrayNBT.class);
                                        case Constants.NBT.TAG_STRING:
                                            return context.serialize(src, StringNBT.class);
                                        case Constants.NBT.TAG_LIST:
                                            return context.serialize(src, ListNBT.class);
                                        case Constants.NBT.TAG_COMPOUND:
                                            return context.serialize(src, CompoundNBT.class);
                                        case Constants.NBT.TAG_INT_ARRAY:
                                            return context.serialize(src, IntArrayNBT.class);
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
                                return ((JsonDeserializer<T>) (json, typeOfT, context) ->
                                {
                                    if (json.isJsonNull()) {
                                        // noinspection unchecked
                                        return (T) NBTUtilBC.NBT_NULL;
                                    }
                                    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                                        Number number = json.getAsJsonPrimitive().getAsNumber();
                                        if (number instanceof BigInteger || number instanceof Long || number instanceof Integer
                                                || number instanceof Short || number instanceof Byte)
                                        {
                                            return context.deserialize(json, LongNBT.class);
                                        } else {
                                            return context.deserialize(json, DoubleNBT.class);
                                        }
                                    }
                                    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
                                        return context.deserialize(
                                                new JsonPrimitive(json.getAsJsonPrimitive().getAsBoolean() ? (byte) 1 : (byte) 0),
                                                ByteNBT.class);
                                    }
                                    if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                                        return context.deserialize(json, StringNBT.class);
                                    }
                                    if (json.isJsonArray()) {
                                        return context.deserialize(json, ListNBT.class);
                                    }
                                    if (json.isJsonObject()) {
                                        return context.deserialize(json, CompoundNBT.class);
                                    }
                                    throw new IllegalArgumentException(json.toString());
                                }).deserialize(Streams.parse(in), type.getType(), gson::fromJson);
                            }
                        } : null;
                    }
                }).registerTypeAdapter(ByteNBT.class,
                        (JsonSerializer<ByteNBT>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsByte()))
                .registerTypeAdapter(ByteNBT.class,
                        (JsonDeserializer<
                                ByteNBT>) (json, typeOfT, context) -> ByteNBT.valueOf(json.getAsJsonPrimitive().getAsByte()))
                .registerTypeAdapter(ShortNBT.class,
                        (JsonSerializer<ShortNBT>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsShort()))
                .registerTypeAdapter(ShortNBT.class,
                        (JsonDeserializer<
                                ShortNBT>) (json, typeOfT, context) -> ShortNBT.valueOf(json.getAsJsonPrimitive().getAsShort()))
                .registerTypeAdapter(IntNBT.class,
                        (JsonSerializer<IntNBT>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsInt()))
                .registerTypeAdapter(IntNBT.class,
                        (JsonDeserializer<
                                IntNBT>) (json, typeOfT, context) -> IntNBT.valueOf(json.getAsJsonPrimitive().getAsInt()))
                .registerTypeAdapter(LongNBT.class,
                        (JsonSerializer<LongNBT>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsLong()))
                .registerTypeAdapter(LongNBT.class,
                        (JsonDeserializer<
                                LongNBT>) (json, typeOfT, context) -> LongNBT.valueOf(json.getAsJsonPrimitive().getAsLong()))
                .registerTypeAdapter(FloatNBT.class,
                        (JsonSerializer<FloatNBT>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsFloat()))
                .registerTypeAdapter(FloatNBT.class,
                        (JsonDeserializer<
                                FloatNBT>) (json, typeOfT, context) -> FloatNBT.valueOf(json.getAsJsonPrimitive().getAsFloat()))
                .registerTypeAdapter(DoubleNBT.class,
                        (JsonSerializer<DoubleNBT>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsDouble()))
                .registerTypeAdapter(DoubleNBT.class,
                        (JsonDeserializer<DoubleNBT>) (json, typeOfT,
                                                       context) -> DoubleNBT.valueOf(json.getAsJsonPrimitive().getAsDouble()))
                .registerTypeAdapter(ByteArrayNBT.class, (JsonSerializer<ByteArrayNBT>) (src, typeOfSrc, context) ->
                {
                    JsonArray jsonArray = new JsonArray();
                    for (byte element : src.getAsByteArray()) {
                        jsonArray.add(new JsonPrimitive(element));
                    }
                    return jsonArray;
                })
                .registerTypeAdapter(ByteArrayNBT.class,
                        (JsonDeserializer<ByteArrayNBT>) (json, typeOfT, context) -> new ByteArrayNBT(
                                ArrayUtils.toPrimitive(StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                                        .map(JsonElement::getAsByte).toArray(Byte[]::new))))
                .registerTypeAdapter(StringNBT.class,
                        (JsonSerializer<StringNBT>) (src, typeOfSrc, context) -> new JsonPrimitive(src.getAsString()))
                .registerTypeAdapter(StringNBT.class,
                        (JsonDeserializer<StringNBT>) (json, typeOfT,
                                                       context) -> StringNBT.valueOf(json.getAsJsonPrimitive().getAsString()))
                .registerTypeAdapter(ListNBT.class, (JsonSerializer<ListNBT>) (src, typeOfSrc, context) ->
                {
                    JsonArray jsonArray = new JsonArray();
                    for (int i = 0; i < src.size(); i++) {
                        INBT element = src.get(i);
                        jsonArray.add(context.serialize(element, INBT.class));
                    }
                    return jsonArray;
                }).registerTypeAdapter(ListNBT.class, (JsonDeserializer<ListNBT>) (json, typeOfT, context) ->
                {
                    ListNBT nbtTagList = new ListNBT();
                    StreamSupport.stream(json.getAsJsonArray().spliterator(), false)
                            .map(element -> context.<INBT>deserialize(element, INBT.class))
                            .forEach(nbtTagList::add);
                    return nbtTagList;
                }).registerTypeAdapter(CompoundNBT.class, (JsonSerializer<CompoundNBT>) (src, typeOfSrc, context) ->
                {
                    JsonObject jsonObject = new JsonObject();
                    for (String key : src.getAllKeys()) {
                        jsonObject.add(key, context.serialize(src.get(key), INBT.class));
                    }
                    return jsonObject;
                })
                .registerTypeAdapter(CompoundNBT.class, (JsonDeserializer<CompoundNBT>) (json, typeOfT, context) ->
                {
                    CompoundNBT nbtTagCompound = new CompoundNBT();
                    for (Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                        nbtTagCompound.put(entry.getKey(), context.deserialize(entry.getValue(), INBT.class));
                    }
                    return nbtTagCompound;
                }).registerTypeAdapter(IntArrayNBT.class, (JsonSerializer<IntArrayNBT>) (src, typeOfSrc, context) ->
                {
                    JsonArray jsonArray = new JsonArray();
                    for (int element : src.getAsIntArray()) {
                        jsonArray.add(new JsonPrimitive(element));
                    }
                    return jsonArray;
                }).registerTypeAdapter(IntArrayNBT.class,
                        (JsonDeserializer<IntArrayNBT>) (json, typeOfT, context) -> new IntArrayNBT(StreamSupport
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

    // Calen

    public static JsonElement serializeFluidStack(FluidStack fluidStack) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", fluidStack.getFluid().getRegistryName().toString());
        json.addProperty("amount", fluidStack.getAmount());
        return json;
    }

    public static FluidStack deSerializeFluidStack(JsonObject json) {
        String fluidId = JSONUtils.getAsString(json, "fluid");
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId));
        int amount = JSONUtils.getAsInt(json, "amount");
        return new FluidStack(fluid, amount);
    }

    public static JsonElement serializeItemStack(ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("item", stack.getItem().getRegistryName().toString());
        if (stack.getCount() > 1) {
            json.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            json.addProperty("nbt", stack.getTag().toString());
        }
        return json;
    }

    public static ItemStack deSerializeItemStack(JsonObject json) {
        String itemId = JSONUtils.getAsString(json, "item");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        int count = 1;
        if (json.has("count")) {
            count = JSONUtils.getAsInt(json, "count");
        }
        CompoundNBT nbt = JsonUtils.readNBT(json, "nbt");
        ItemStack ret = new ItemStack(item, count);
        if (nbt != null) {
            ret.setTag(nbt);
        }
        return ret;
    }

    public static JsonElement serializeIngredientStack(IngredientStack stack) {
        JsonObject json = new JsonObject();
        json.add("ingredient", stack.ingredient.toJson());
        json.addProperty("count", stack.count);
        return json;
    }

    public static IngredientStack deSerializeIngredientStack(JsonObject json) {
        Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
        int count = JSONUtils.getAsInt(json, "count");
        return new IngredientStack(ingredient, count);
    }
}
