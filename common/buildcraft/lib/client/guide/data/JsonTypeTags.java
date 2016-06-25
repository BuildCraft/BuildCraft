package buildcraft.lib.client.guide.data;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import net.minecraft.util.StringUtils;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.ETypeTag;
import buildcraft.lib.client.guide.TypeOrder;

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

    public String[] getOrdered(TypeOrder typeOrder) {
        String[] strings = new String[typeOrder.tags.size()];
        for (int i = 0; i < strings.length; i++) {
            ETypeTag tag = typeOrder.tags.get(i);
            strings[i] = getTyped(tag);
        }
        return strings;
    }

    private String getTyped(ETypeTag tag) {
        String typed;
        if (tag == ETypeTag.MOD) {
            typed = mod;
        } else if (tag == ETypeTag.SUB_MOD) {
            typed = subMod;
        } else if (tag == ETypeTag.TYPE) {
            typed = type;
        } else if (tag == ETypeTag.SUB_TYPE) {
            typed = subType;
        } else {
            throw new IllegalStateException("Don't know the type " + tag);
        }
        return tag.preText + typed;
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
