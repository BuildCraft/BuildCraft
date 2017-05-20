package buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

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
            throw new JsonSyntaxException("Something was wrong with " + obj + " when deserialzing it as a " + token, ise);
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
            throw new JsonSyntaxException("Something was wrong with " + obj + " when deserialzing it as a " + token, ise);
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
}
