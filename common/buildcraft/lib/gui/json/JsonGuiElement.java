package buildcraft.lib.gui.json;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.json.JsonGuiIterator.ResolvedIterator;
import buildcraft.lib.json.JsonVariableObject;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class JsonGuiElement extends JsonVariableObject {
    public final String name;
    public final String fullName;
    /** Map of string {@code ->} property. Non-primitives are expanded, so arrays are turned into key[index], and objects are
     * turned into key.child.
     * <p>
     * For example: "size": [25, 30] is added to the map as two entries: "size[0]"="25", and "size[1]"="30". "data":
     * {"prop": "" */
    public final Map<String, String> properties = new LinkedHashMap<>();
    @Nullable
    public final JsonGuiIterator iterator;
    public final FunctionContext context;
    public final JsonObject json;
    private final Map<String, JsonGuiElement> types;

    private JsonGuiElement(String name, String fullName, FunctionContext context, JsonObject json,
                           ResolvedIterator iter) {
        this.name = name;
        this.fullName = fullName;
        this.context = new FunctionContext(context);
        iter.putProperties(context, properties);
        this.json = json;
        iterator = null;
        types = new LinkedHashMap<>();
        if (json.has("variables") && json.get("variables").isJsonObject()) {
            putVariables(json.getAsJsonObject("variables"), this.context);
        }
        finaliseVariables();
    }

    public JsonGuiElement(JsonObject json, String name, String fullName, Map<String, JsonGuiElement> typeLookup,
                          FunctionContext context) {
        try {
            this.json = json;
            this.name = name;
            this.fullName = fullName;
            this.context = new FunctionContext(context);
            this.types = typeLookup;

//            String str = JsonUtils.getString(json, "type", null);
            String str = JSONUtils.getAsString(json, "type", null);
            if (str != null) {
                JsonGuiElement parent = typeLookup.get(str);
                if (parent != null) {
                    properties.putAll(parent.properties);
                }
            }
            for (Entry<String, JsonElement> entry : json.entrySet()) {
                String key = entry.getKey();
                if ("type".equals(key) && properties.containsKey("type")) {
                    continue;
                }
                JsonElement value = entry.getValue();
                putProperties(key, value);
            }
            if (json.has("iterator")) {
                iterator = new JsonGuiIterator(json.get("iterator"));
            } else {
                iterator = null;
                if (json.has("variables") && json.get("variables").isJsonObject()) {
                    putVariables(json.getAsJsonObject("variables"), this.context);
                }
            }
            finaliseVariables();
        } catch (JsonSyntaxException jse) {
            throw new JsonSyntaxException("Failed to read element " + name, jse);
        }
    }

    private void putProperties(String key, JsonElement value) {
        if (value.isJsonPrimitive()) {
            properties.put(key, value.getAsString());
        } else if (value.isJsonArray()) {
            JsonArray array = value.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                putProperties(key + "[" + i + "]", array.get(i));
            }
        } else if (value.isJsonObject()) {
            JsonObject sub = value.getAsJsonObject();
            for (Entry<String, JsonElement> entry : sub.entrySet()) {
                putProperties(key + "." + entry.getKey(), entry.getValue());
            }
        } else if (value.isJsonNull()) {
            properties.put(key, "null");
        }
    }

    public List<JsonGuiElement> iterate(FunctionContext fnCtx) {
        List<JsonGuiElement> list = new ArrayList<>();
        if (iterator == null) {
            list.add(this);
        } else {
            ResolvedIterator resolvedIterator = iterator.new ResolvedIterator(fnCtx);
            if (resolvedIterator.start()) {
                do {
                    JsonGuiElement elem = new JsonGuiElement(name, fullName, context, json, resolvedIterator);
                    elem.types.putAll(types);
                    elem.properties.putAll(properties);
                    list.add(elem);
                }
                while (!resolvedIterator.iterate());
            }
        }
        return list;
    }

    public List<JsonGuiElement> getChildren(String subName) {
        JsonElement chElem = json.get(subName);
        if (chElem == null || !chElem.isJsonObject()) {
            return ImmutableList.of();
        }
        JsonObject chObject = chElem.getAsJsonObject();
        List<JsonGuiElement> list = new ArrayList<>();

        for (Entry<String, JsonElement> key : chObject.entrySet()) {
            String chName = key.getKey();
            JsonElement value = key.getValue();
            list.add(getChildElement(chName, value));
        }
        return list;
    }

    public JsonGuiElement getChildElement(String childName, JsonElement elem) {
        JsonElement value = elem;
        if (!value.isJsonObject()) {
            throw new JsonSyntaxException("Expected an object, got " + value);
        }
        JsonObject childObject = value.getAsJsonObject();
        return new JsonGuiElement(childObject, childName, fullName + "." + childName, types, context);
    }

    @Override
    public String toString() {
        return "JsonGuiElement[ " + properties + " ]";
    }

    public void printOut(Consumer<String> logger) {
        Consumer<String> log2 = s -> logger.accept("  " + s);
        logger.accept(name + ":");
        for (String key : properties.keySet()) {
            log2.accept(key + " = " + properties.get(key));
        }
    }
}
