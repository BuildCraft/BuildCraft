package buildcraft.lib.gui.config;

import buildcraft.api.core.BCLog;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

class GuiConfigSet {
    final Map<String, GuiConfigEntry> properties = new TreeMap<>();

    IVariableNode getOrAddProperty(String name, IExpressionNode value) {
        return properties.computeIfAbsent(name, GuiConfigEntry::new).getOrAdd(value);
    }

    JsonObject writeToJson() {
        JsonObject json = new JsonObject();
        for (Entry<String, GuiConfigEntry> entry : properties.entrySet()) {
            String name = entry.getKey();
            GuiConfigEntry value = entry.getValue();
            json.add(name, value.writeToJson());
        }
        return json;
    }

    void readFromJson(JsonObject json) {
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            String name = entry.getKey();
            GuiConfigEntry guiEntry = properties.get(name);
            if (guiEntry == null) {
                guiEntry = new GuiConfigEntry(name);
                properties.put(name, guiEntry);
            }
            JsonElement elem = entry.getValue();
            if (!elem.isJsonObject()) {
                BCLog.logger.warn("[lib.gui.config] Found a non-object element in '" + name + "'");
                continue;
            }
            guiEntry.readFromJson(elem.getAsJsonObject());
        }
    }
}
