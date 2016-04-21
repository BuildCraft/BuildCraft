package buildcraft.lib.bpt.json;

import java.lang.reflect.Type;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;

/** Represents a collection of Json based Schematics. */
public class JsonSchematicSet {
    public static Gson createParser() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(JsonSchematicSet.class, (JsonDeserializer<JsonSchematicSet>) JsonSchematicSet::new);
        builder.registerTypeAdapter(JsonSchematicBlock.class, (JsonDeserializer<JsonSchematicBlock>) JsonSchematicBlock::new);
        builder.registerTypeAdapter(JsonNBTData.class, (JsonDeserializer<JsonNBTData>) JsonNBTData::new);
        return builder.create();
    }

    public final ImmutableList<JsonSchematicBlock> schematics;

    public JsonSchematicSet(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

    }
}
