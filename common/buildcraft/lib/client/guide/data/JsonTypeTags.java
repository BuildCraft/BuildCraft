package buildcraft.lib.client.guide.data;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import net.minecraft.util.StringUtils;

import buildcraft.api.core.BCLog;

public class JsonTypeTags {
    public static final JsonTypeTags EMPTY = new JsonTypeTags("", "", "", "");

    public final String mod;
    public final String type;

    @SerializedName("sub_mod")
    public final String subMod;

    @SerializedName("sub_type")
    public final String subType;

    public JsonTypeTags(String mod, String subMod, String type, String subType) {
        this.mod = mod;
        this.subMod = subMod;
        this.type = type;
        this.subType = subType;
    }

    public JsonTypeTags inheritMissingTags(JsonTypeTags parent) {
        String mod = firstNonEmpty(this.mod, parent.mod, "unknown");
        String subMod = firstNonEmpty(this.subMod, parent.subMod);
        String type = firstNonEmpty(this.type, parent.type, "unknown");
        String subType = firstNonEmpty(this.subType, parent.subType, "unknown");
        return new JsonTypeTags(mod, subMod, type, subType);
    }

    private static String firstNonEmpty(String... strings) {
        String current = null;
        for (int i = 0; i < strings.length; i++) {
            current = strings[i];
            if (!StringUtils.isNullOrEmpty(current)) {
                break;
            }
        }
        return current;
    }

    public void printContents(int indent) {
        String f = "";
        while (indent > 0) {
            f += "  ";
            indent--;
        }
        BCLog.logger.info(f + "mod = " + mod + ",");
        BCLog.logger.info(f + "sub_mod = " + subMod + ",");
        BCLog.logger.info(f + "type = " + type + ",");
        BCLog.logger.info(f + "sub_type = " + subType);
    }

    public Map<String, String> createMap() {
        Map<String, String> map = new HashMap<>();
        map.put("mod", mod);
        map.put("sub_mod", subMod);
        map.put("type", type);
        map.put("sub_type", subType);

        return map;
    }
}
