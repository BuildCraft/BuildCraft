package buildcraft.lib.bpt.json;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import buildcraft.lib.misc.JsonUtil;

/** Represents a collection of Json based Schematics. */
public class JsonSchematicSet {
    public static Gson createParser() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(JsonSchematicSet.class, (JsonDeserializer<JsonSchematicSet>) JsonSchematicSet::new);
        builder.registerTypeAdapter(JsonSchematicBlock.class, (JsonDeserializer<JsonSchematicBlock>) JsonSchematicBlock::new);
        builder.registerTypeAdapter(JsonNBTData.class, (JsonDeserializer<JsonNBTData>) JsonNBTData::new);
        builder.registerTypeAdapter(JsonItemStack.class, (JsonDeserializer<JsonItemStack>) JsonItemStack::new);
        return builder.create();
    }

    public final ImmutableList<JsonSchematicBlock> schematics;

    public JsonSchematicSet(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        schematics = JsonUtil.getSubAsImmutableList(json.getAsJsonObject(), "schematics", new TypeToken<ArrayList<JsonSchematicBlock>>() {}, context);
    }
}
