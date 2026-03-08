package buildcraft.lib.gui.config;

import buildcraft.api.core.BCLog;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.NodeTypes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

class GuiConfigEntry {
    final String name;
    final Map<String, GuiProperty> typeToProperty = new TreeMap<>();

    GuiConfigEntry(String name) {
        this.name = name;
    }

    GuiProperty getOrAdd(IExpressionNode value) {
        String type = NodeTypes.getName(NodeTypes.getType(value));
        GuiProperty prop = typeToProperty.get(type);
        if (prop == null) {
            GuiPropertyConstructor constructor = GuiConfigManager.customGuiProperties.get(type);
            if (constructor == null) {
                throw new IllegalArgumentException("No support for '" + type
                        + "' has been added!\n\tSupported types are: " + GuiConfigManager.customGuiProperties.keySet());
            }
            prop = constructor.create(name);
            prop.set(value);
            typeToProperty.put(type, prop);
            GuiConfigManager.markDirty();
        }
        return prop;
    }

    JsonObject writeToJson() {
        JsonObject json = new JsonObject();
        for (Entry<String, GuiProperty> entry : typeToProperty.entrySet()) {
            json.add(entry.getKey(), entry.getValue().writeToJson());
        }
        return json;
    }

    void readFromJson(JsonObject json) {
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            String type = entry.getKey();
            JsonElement elem = entry.getValue();
            Class<?> clazz = NodeTypes.getType(type);
            if (clazz == null) {
                BCLog.logger.warn("[lib.gui.config] Unknown NodeType '" + type + "' - must be one of "
                        + NodeTypes.getValidTypeNames());
                continue;
            }
            GuiProperty current = typeToProperty.get(type);
            if (current == null) {
                current = getOrAdd(NodeTypes.makeVariableNode(clazz, name));
            }
            current.readFromJson(elem);
        }
    }
}
