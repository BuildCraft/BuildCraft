/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.data;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.ETypeTag;
import buildcraft.lib.client.guide.TypeOrder;
import net.minecraft.util.StringUtil;

public class JsonTypeTags {
    public static final JsonTypeTags EMPTY = new JsonTypeTags("", "", "");

    public final String domain;
    public final String type;
    public final String subType;

    public JsonTypeTags(String domain, String type, String subType) {
        this.domain = domain;
        this.type = type;
        this.subType = subType;
    }

    public JsonTypeTags(String type) {
        this(null, type, null);
    }

    public String[] getOrdered(TypeOrder typeOrder) {
        if (domain == null && subType == null) {
            // Built-in type for "others"
            return new String[] { type };
        }

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
            typed = getMod(domain, 0);
        } else if (tag == ETypeTag.SUB_MOD) {
            typed = getMod(domain, 1);
        } else if (tag == ETypeTag.TYPE) {
            typed = type;
        } else if (tag == ETypeTag.SUB_TYPE) {
            typed = subType;
        } else {
            throw new IllegalStateException("Don't know the type " + tag);
        }
        return tag.preText + typed;
    }

    private static String getMod(String domain, int index) {
        if (domain.startsWith("buildcraft")) {
            return index == 0 ? "buildcraft" : domain.substring("buildcraft".length());
        }
        return index == 0 ? domain : "";
    }

    public JsonTypeTags inheritMissingTags(JsonTypeTags parent) {
        String d = firstNonEmpty(this.domain, parent.domain, "unknown");
        String t = firstNonEmpty(this.type, parent.type, "unknown");
        String st = firstNonEmpty(this.subType, parent.subType, "unknown");
        return new JsonTypeTags(d, t, st);
    }

    private static String firstNonEmpty(String... strings) {
        String current = null;
        for (String string : strings) {
            current = string;
            if (!StringUtil.isNullOrEmpty(current)) {
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
        BCLog.logger.info(f + "domain = " + domain + ",");
        BCLog.logger.info(f + "type = " + type + ",");
        BCLog.logger.info(f + "sub_type = " + subType);
    }
}
