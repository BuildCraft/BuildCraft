package buildcraft.lib.gui.json;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.json.JsonVariableObject;
import buildcraft.lib.misc.JsonUtil;

public class JsonGuiInfo extends JsonVariableObject {
    public final String sizeX;
    public final String sizeY;
    public final String defaultTexture;
    public final Map<String, JsonGuiElement> types = new HashMap<>();
    public final List<JsonGuiElement> elements = new ArrayList<>();

    public JsonGuiInfo(JsonObject json, FunctionContext fnCtx, ResourceLoaderContext loadHistory) {
        if (json.has("values")) {
            putVariables(JsonUtils.getJsonObject(json, "values"), fnCtx);
        }

        if (json.has("elements_below")) {
            JsonObject jElems = JsonUtils.getJsonObject(json, "elements_below");
            for (Entry<String, JsonElement> entry : jElems.entrySet()) {
                String name = entry.getKey();
                JsonObject obj = (JsonObject) entry.getValue();
                JsonGuiElement elem = new JsonGuiElement(obj, name, name, types, fnCtx);
                elements.addAll(elem.iterate(fnCtx));
            }
        }

        if (json.has("parent")) {
            String parent = JsonUtils.getString(json, "parent");
            ResourceLocation location = new ResourceLocation(parent + ".json");
            try (InputStreamReader reader = loadHistory.startLoading(location)) {
                JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
                JsonGuiInfo info = new JsonGuiInfo(obj, fnCtx, loadHistory);
                types.putAll(info.types);
                elements.addAll(info.elements);
                variables.putAll(info.variables);
            } catch (Exception e) {
                throw new JsonSyntaxException("Failed to load parent " + parent, e);
            } finally {
                loadHistory.finishLoading();
            }
        }

        if (json.has("variables")) {
            putVariables(JsonUtils.getJsonObject(json, "variables"), fnCtx);
        }

        if (json.has("size")) {
            JsonElement size = json.get("size");
            sizeX = size.getAsJsonArray().get(0).getAsString();
            sizeY = size.getAsJsonArray().get(1).getAsString();
        } else {
            throw new JsonSyntaxException("Expected size as an array!");
        }
        defaultTexture = JsonUtil.getAsString(json.get("texture"));
        if (json.has("types")) {
            JsonObject jTypes = JsonUtils.getJsonObject(json, "types");
            for (Entry<String, JsonElement> entry : jTypes.entrySet()) {
                String name = entry.getKey();
                JsonObject obj = (JsonObject) entry.getValue();
                types.put(name, new JsonGuiElement(obj, name, name, types, fnCtx));
            }
        }
        if (json.has("elements")) {
            JsonObject jElems = JsonUtils.getJsonObject(json, "elements");
            for (Entry<String, JsonElement> entry : jElems.entrySet()) {
                String name = entry.getKey();
                JsonObject obj = (JsonObject) entry.getValue();
                JsonGuiElement elem = new JsonGuiElement(obj, name, name, types, fnCtx);
                elements.addAll(elem.iterate(fnCtx));
            }
        }
        finaliseVariables();
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
