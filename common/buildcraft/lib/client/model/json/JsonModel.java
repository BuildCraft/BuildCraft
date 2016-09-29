package buildcraft.lib.client.model.json;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.util.JsonUtils;

import buildcraft.lib.misc.JsonUtil;

/** {@link ModelBlock} but with different/additional features */
public class JsonModel {
    public static final Gson SERIALISER;

    public final boolean ambientOcclusion;
    public final Map<String, String> textures;
    public final JsonModelPart[] cutoutElements, translucentElements;

    static {
        SERIALISER = new GsonBuilder()//
                .registerTypeAdapter(JsonModel.class, (JsonDeserializer<JsonModel>) JsonModel::new)//
                .create();
    }

    public static JsonModel deserialize(String jsonString) throws JsonSyntaxException {
        return deserialize(new StringReader(jsonString));
    }

    public static JsonModel deserialize(Reader reader) throws JsonSyntaxException {
        return SERIALISER.fromJson(reader, JsonModel.class);
    }

    public static JsonModelPart deserializePart(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
        // TODO: add different classes based on type
        return new JsonModelPart(json, context);
    }

    private static JsonModelPart[] deserializePartArray(JsonDeserializationContext context, JsonObject json, String member) {
        if (!json.has(member)) {
            throw new JsonSyntaxException("Did not have '" + member + "' in '" + json + "'");
        }
        JsonElement elem = json.get(member);
        if (!elem.isJsonArray()) {
            throw new JsonSyntaxException("Expected an array, got '" + elem + "'");
        }
        JsonArray array = elem.getAsJsonArray();
        JsonModelPart[] to = new JsonModelPart[array.size()];
        for (int i = 0; i < to.length; i++) {
            to[i] = deserializePart(array.get(i), context);
        }
        return to;
    }

    public JsonModel(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            ambientOcclusion = JsonUtils.getBoolean(obj, "ambientocclusion", false);
            textures = JsonUtil.getSubAsImmutableMap(obj, "textures", new TypeToken<HashMap<String, String>>() {}, context);
            if (obj.has("elements")) {
                cutoutElements = deserializePartArray(context, obj, "elements");
                translucentElements = new JsonModelPart[0];
            } else {
                cutoutElements = deserializePartArray(context, obj, "cutout");
                translucentElements = deserializePartArray(context, obj, "translucent");
            }
        } else {
            throw new JsonSyntaxException("Excepted an object, got " + json);
        }
    }
}
