package buildcraft.lib.gui.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.JsonUtils;

public class JsonGuiElement {
    public final String name;
    /** Map of string -> property. non-primitives are expanded, so arrays are turned into key[index], and objects are
     * turned into key.child.
     * <p>
     * For example: "size": [25, 30] is added to the map as two entries: "size[0]"="25", and "size[1]"="30". "data":
     * {"prop": "" */
    public final Map<String, String> properties = new LinkedHashMap<>();

    public JsonGuiElement(String name, Map<String, String> properties) {
        this.name = name;
        this.properties.putAll(properties);
    }

    public JsonGuiElement(JsonObject json, String name, Map<String, JsonGuiElement> typeLookup) {
        this.name = name;
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

    public List<JsonGuiElement> iterate() {
        List<JsonGuiElement> list = new ArrayList<>();
        if (!properties.containsKey("iterator")) {
            list.add(this);
        } else {
            String iter = properties.get("iterator");
            // TODO: Parse this properly!
            String iterName = iter.substring(0, iter.indexOf('=')).trim();
            String bounds = iter.substring(iter.indexOf('=') + 1);
            String lower = bounds.substring(0, bounds.indexOf(',')).trim().replace(" ", "");
            String upper = bounds.substring(bounds.indexOf(',') + 1).trim().replace(" ", "");
            int l = Integer.parseInt(lower.substring(1)) + (lower.startsWith("(") ? 1 : 0);
            int u = Integer.parseInt(upper.substring(0, upper.length() - 1)) - (upper.endsWith(")") ? 1 : 0);
            if (u >= l) {
                for (int i = l; i <= u; i++) {
                    JsonGuiElement elem = new JsonGuiElement(name, properties);
                    elem.properties.put(iterName, Integer.toString(i));
                    list.add(elem);
                }
            }
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
