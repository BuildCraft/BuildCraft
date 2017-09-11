package buildcraft.lib.gui.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.JsonUtils;

import buildcraft.api.core.BCLog;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.gui.json.JsonGuiIterator.ResolvedIterator;

public class JsonGuiElement {
    public final String name;
    public final String fullName;
    /** Map of string -> property. non-primitives are expanded, so arrays are turned into key[index], and objects are
     * turned into key.child.
     * <p>
     * For example: "size": [25, 30] is added to the map as two entries: "size[0]"="25", and "size[1]"="30". "data":
     * {"prop": "" */
    public final Map<String, String> properties = new LinkedHashMap<>();
    @Nullable
    public final JsonGuiIterator iterator;
    public final FunctionContext context;

    private JsonGuiElement(String name, String fullName, Map<String, String> properties, JsonGuiIterator iter,
        FunctionContext context) {
        this.name = name;
        this.fullName = fullName;
        this.properties.putAll(properties);
        this.iterator = iter;
        this.context = new FunctionContext(context);
    }

    public JsonGuiElement(JsonObject json, String name, String fullName, Map<String, JsonGuiElement> typeLookup,
        FunctionContext context) {
        this.name = name;
        this.fullName = fullName;
        this.context = new FunctionContext(context);
        String str = JsonUtils.getString(json, "type", null);
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
                    JsonGuiElement elem = new JsonGuiElement(name, fullName, properties, null, context);
                    resolvedIterator.putProperties(elem.context);
                    list.add(elem);
                } while (!resolvedIterator.iterate());
            } else {
                BCLog.logger
                    .info("[lib.gui.json] Skipping " + fullName + " as its condition didn't include its start!");
            }
        }
        return list;
    }

    public List<JsonGuiElement> getChildren(JsonGuiInfo info, String subName) {
        List<JsonGuiElement> list = new ArrayList<>();
        List<String> childKeys = new ArrayList<>();
        Table<String, String, String> allChildren = HashBasedTable.create();
        Map<String, String> parentProperties = new HashMap<>();
        for (Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            parentProperties.put("parent." + key, value);
            if (!key.startsWith(subName)) {
                continue;
            }
            String subKey = key.substring(subName.length() + 1);
            int indexOf = subKey.indexOf('.');
            String childName = subKey.substring(0, indexOf);
            String childPropName = subKey.substring(indexOf + 1);
            if (!childKeys.contains(childName)) {
                childKeys.add(childName);
            }
            allChildren.put(childName, childPropName, value);
        }

        for (String childName : childKeys) {
            Map<String, String> childProperties = new HashMap<>(allChildren.row(childName));
            childProperties.putAll(parentProperties);
            String type = childProperties.get("type");
            JsonGuiElement parent = info.types.get(type);
            if (parent != null) {
                Map<String, String> props2 = new HashMap<>();
                props2.putAll(parent.properties);
                props2.putAll(childProperties);
                props2.put("type", parent.properties.get("type"));
                childProperties = props2;
            }
            // TODO: Allow children to iterate!
            list.add(new JsonGuiElement(childName, fullName + "." + childName, childProperties, null, context));
        }
        return list;
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
