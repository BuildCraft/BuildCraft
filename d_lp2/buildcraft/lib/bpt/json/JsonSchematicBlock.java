package buildcraft.lib.bpt.json;

import java.lang.reflect.Type;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;

/** A single block type for schematics. */
public class JsonSchematicBlock {
    public final String registryName, ignoreDrops, placedBlock;
    public final JsonNBTData tileNbt = null;
    public final ImmutableList<String> requirements;
    public final ImmutableList<String> permissionsRequired;

    public JsonSchematicBlock(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            // TODO :)
        } else {
            throw new JsonSyntaxException("Not an object!");
        }
    }
}
