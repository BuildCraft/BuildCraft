package buildcraft.lib.bpt.json;

import java.lang.reflect.Type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class JsonNBTData {
    /** NBT tags that are */
    public final ImmutableList<String> discarded;
    public final ImmutableList<String> kept;
    public final ImmutableList<String> items;
    public final ImmutableList<String> fluids;
    public final ImmutableMap<String, JsonNBTData> subTags;

    public JsonNBTData(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();

            if (obj.has("discarded") && obj.has("kept")) {
                throw new JsonSyntaxException("Cannot both keep and discard tags!");
            } else if (obj.has("discarded")) {
                this.kept = ImmutableList.of();
                this.discarded = context.deserialize(obj.get("discarded"), new TypeToken<ImmutableList<String>>() {}.getType());
                obj.remove("discarded");
            } else {
                this.discarded = ImmutableList.of();
                this.kept = context.deserialize(obj.get("kept"), new TypeToken<ImmutableList<String>>() {}.getType());
                obj.remove("kept");
            }

            if (obj.has("items")) {
                JsonElement jsonItems = obj.get("items");
                ImmutableList.Builder<String> items = ImmutableList.builder();

                this.items = items.build();
                obj.remove("items");
            } else {
                items = ImmutableList.of();
            }

            if (obj.has("fluids")) {
                JsonElement jsonFluids = obj.get("fluids");
                ImmutableList.Builder<String> fluids = ImmutableList.builder();

                this.fluids = fluids.build();
                obj.remove("fluids");
            } else {
                fluids = ImmutableList.of();
            }

            if (obj.has("subTags")) {
                JsonElement jsonSubTags = obj.get("jsonSubTags");
                ImmutableMap.Builder<String, JsonNBTData> subTags = ImmutableMap.builder();

                this.subTags = subTags.build();
                obj.remove("subTags");
            } else {
                subTags = ImmutableMap.of();
            }
        } else {
            throw new JsonSyntaxException("Must be an object!");
        }
    }
}
