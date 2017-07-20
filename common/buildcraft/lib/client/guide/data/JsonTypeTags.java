/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.data;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.ETypeTag;
import buildcraft.lib.client.guide.TypeOrder;
import com.google.gson.annotations.SerializedName;
import net.minecraft.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

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
        String m = firstNonEmpty(this.mod, parent.mod, "unknown");
        String sm = firstNonEmpty(this.subMod, parent.subMod);
        String t = firstNonEmpty(this.type, parent.type, "unknown");
        String st = firstNonEmpty(this.subType, parent.subType, "unknown");
        return new JsonTypeTags(m, sm, t, st);
    }

    private static String firstNonEmpty(String... strings) {
        String current = null;
        for (String string : strings) {
            current = string;
            if (!StringUtils.isNullOrEmpty(current)) {
                break;
            }
        }
        return current;
    }

    public void printContents(int indent) {
        StringBuilder f = new StringBuilder();
        while (indent > 0) {
            f.append("  ");
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
