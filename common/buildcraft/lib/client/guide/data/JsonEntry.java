/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.data;

import com.google.gson.annotations.SerializedName;

import buildcraft.api.core.BCLog;

public class JsonEntry {
    public final String title;
    public final String page;
    public final String type;
    public final String source;

    @SerializedName("item_stack")
    public final String itemStack;

    @SerializedName("type_tags")
    public final JsonTypeTags typeTags;

    public JsonEntry(String title, String page, String type, String source, String itemStack, JsonTypeTags typeTags) {
        this.title = title;
        this.page = page;
        this.type = type;
        this.source = source;
        this.itemStack = itemStack;
        this.typeTags = typeTags;
    }

    public JsonEntry inherit(JsonTypeTags parent, String entryMask) {
        JsonTypeTags tags;
        if (this.typeTags == null) {
            tags = parent;
        } else {
            tags = this.typeTags.inheritMissingTags(parent);
        }

        // apply mask
        String realPage = this.page;
        realPage = entryMask.replaceAll("<page>", realPage);
        realPage = realPage.replaceAll("<mod>", tags.mod);
        realPage = realPage.replaceAll("<type>", tags.type);
        realPage = realPage.replaceAll("<sub_mod>", tags.subMod);
        realPage = realPage.replaceAll("<sub_type>", tags.subType);
        return new JsonEntry(title, realPage, type, source, itemStack, tags);
    }

    public void printContents() {
        BCLog.logger.info("      title = " + title + ",");
        BCLog.logger.info("      page = " + page + ",");
        BCLog.logger.info("      item_stack = " + itemStack + ",");
        BCLog.logger.info("      type = " + type + ",");
        BCLog.logger.info("      source = " + source + ",");
        if (typeTags == null) {
            BCLog.logger.info("      type_tags = null");
        } else {
            BCLog.logger.info("      type_tags = {");
            typeTags.printContents(4);
            BCLog.logger.info("      }");
        }
    }
}
