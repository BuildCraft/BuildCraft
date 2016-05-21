package buildcraft.lib.bpt.json;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import net.minecraft.util.JsonUtils;

import buildcraft.lib.misc.JsonUtil;

/** A single block type for schematics. */
public class JsonSchematicBlock {
    public final String registryName;
    public final JsonBlockState placedBlock;
    public final boolean ignore, ignoreDrops;
    public final JsonNBTData tileNbt;
    public final ImmutableList<String> requirements;
    public final ImmutableList<String> permissionsRequired;

    public JsonSchematicBlock(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            registryName = JsonUtils.getString(obj, "registryName");
            placedBlock = new JsonBlockState(registryName, obj, "placedBlock", context);
            ignore = JsonUtils.getBoolean(obj, "ignore", false);
            ignoreDrops = JsonUtils.getBoolean(obj, "ignoreDrops", false);
            tileNbt = context.deserialize(json, JsonNBTData.class);
            requirements = JsonUtil.getSubAsImmutableList(obj, "requirements", new TypeToken<ArrayList<String>>() {}, context);
            permissionsRequired = JsonUtil.getSubAsImmutableList(obj, "permissionsRequired", new TypeToken<ArrayList<String>>() {}, context);
        } else {
            throw new JsonSyntaxException("Not an object!");
        }
    }
}
