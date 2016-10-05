package buildcraft.lib.client.model.json;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import com.google.gson.*;

import net.minecraft.util.JsonUtils;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.misc.JsonUtil;

/** {@link JsonModel} but any element can change depening on variables. */
public class JsonVariableModel {
    // Never allow ao or textures to be variable - they need to be hardcoded so that we can stitch them
    public final boolean ambientOcclusion;
    public final Map<String, String> textures;
    public final JsonVariableModelPart[] cutoutElements, translucentElements;

    public static JsonVariableModel deserialize(String jsonString, FunctionContext context) throws JsonSyntaxException {
        return deserialize(new StringReader(jsonString), context);
    }

    public static JsonVariableModel deserialize(Reader reader, FunctionContext context) throws JsonSyntaxException {
        return new JsonVariableModel(new Gson().fromJson(reader, JsonObject.class), context);
    }

    public static JsonVariableModelPart deserializePart(JsonElement json, FunctionContext context) throws JsonParseException {
        // TODO: add different classes based on type
        return JsonVariableModelPart.deserialiseModelPart(json, context);
    }

    private static JsonVariableModelPart[] deserializePartArray(JsonObject json, String member, FunctionContext context) {
        if (!json.has(member)) {
            throw new JsonSyntaxException("Did not have '" + member + "' in '" + json + "'");
        }
        JsonElement elem = json.get(member);
        if (!elem.isJsonArray()) {
            throw new JsonSyntaxException("Expected an array, got '" + elem + "'");
        }
        JsonArray array = elem.getAsJsonArray();
        JsonVariableModelPart[] to = new JsonVariableModelPart[array.size()];
        for (int i = 0; i < to.length; i++) {
            to[i] = deserializePart(array.get(i), context);
        }
        return to;
    }

    public JsonVariableModel(JsonElement json, FunctionContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            ambientOcclusion = JsonUtils.getBoolean(obj, "ambientocclusion", false);
            textures = JsonUtil.deserializeStringMap(obj, "textures");
            if (obj.has("elements")) {
                cutoutElements = deserializePartArray(obj, "elements", context);
                translucentElements = new JsonVariableModelPart[0];
            } else {
                cutoutElements = deserializePartArray(obj, "cutout", context);
                translucentElements = deserializePartArray(obj, "translucent", context);
            }
        } else {
            throw new JsonSyntaxException("Excepted an object, got " + json);
        }
    }
}
