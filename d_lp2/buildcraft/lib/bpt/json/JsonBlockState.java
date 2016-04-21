package buildcraft.lib.bpt.json;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import net.minecraft.util.JsonUtils;

import buildcraft.lib.misc.JsonUtil;

public class JsonBlockState {
    public final String registryName;
    public final ImmutableMap<String, String> state;

    public JsonBlockState(String registryName, JsonObject json, String sub, JsonDeserializationContext context) throws JsonParseException {
        if (!json.has(sub)) {
            this.registryName = registryName;
            this.state = ImmutableMap.of();
        } else {
            this.registryName = JsonUtils.getString(json, "registryName");
            this.state = JsonUtil.getSubAsImmutableMap(json, "state", new TypeToken<HashMap<String, String>>() {}, context);
        }
    }
}
