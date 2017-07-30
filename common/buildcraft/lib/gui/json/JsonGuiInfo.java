package buildcraft.lib.gui.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JsonUtils;

import buildcraft.lib.json.JsonVariableObject;
import buildcraft.lib.misc.JsonUtil;

public class JsonGuiInfo extends JsonVariableObject {
    public final int sizeX;
    public final int sizeY;
    public final String defaultTexture;
    public final Map<String, JsonGuiElement> types = new HashMap<>();
    public final List<JsonGuiElement> elements = new ArrayList<>();

    public JsonGuiInfo(JsonObject json) {

        if (json.has("values")) {

        }

        if (json.has("parent")) {
            // TODO!
        }

        if (json.has("size")) {
            JsonElement size = json.get("size");
            sizeX = size.getAsJsonArray().get(0).getAsInt();
            sizeY = size.getAsJsonArray().get(1).getAsInt();
        } else {
            throw new JsonSyntaxException("Expected size as an array!");
        }
        defaultTexture = JsonUtil.getAsString(json.get("texture"));
        if (json.has("types")) {
            JsonObject jTypes = JsonUtils.getJsonObject(json, "types");
            for (Entry<String, JsonElement> entry : jTypes.entrySet()) {
                String name = entry.getKey();
                JsonObject obj = (JsonObject) entry.getValue();
                types.put(name, new JsonGuiElement(obj, name, name, types));
            }
        }
        if (json.has("elements")) {
            JsonObject jElems = JsonUtils.getJsonObject(json, "elements");
            for (Entry<String, JsonElement> entry : jElems.entrySet()) {
                String name = entry.getKey();
                JsonObject obj = (JsonObject) entry.getValue();
                JsonGuiElement elem = new JsonGuiElement(obj, name, name, types);
                elements.addAll(elem.iterate());
            }
        }
    }

    public void printOut(Consumer<String> logger) {
        logger.accept("size = [ " + sizeX + ", " + sizeY + " ]");
        logger.accept("defaultTexture = " + defaultTexture);
        logger.accept("types:");
        Consumer<String> log2 = s -> logger.accept("  " + s);
        for (JsonGuiElement elem : types.values()) {
            elem.printOut(log2);
        }

        logger.accept("elements:");
        for (JsonGuiElement elem : elements) {
            elem.printOut(log2);
        }
    }
}
