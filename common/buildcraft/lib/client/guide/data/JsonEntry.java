package buildcraft.lib.client.guide.data;

import com.google.gson.annotations.SerializedName;

import buildcraft.api.core.BCLog;

public class JsonEntry {
    public final String title;
    public final String page;

    @SerializedName("item_stack")
    public final String itemStack;

    @SerializedName("type_tags")
    public final JsonTypeTags typeTags;

    public JsonEntry(String title, String page, String itemStack, JsonTypeTags typeTags) {
        this.title = title;
        this.page = page;
        this.itemStack = itemStack;
        this.typeTags = typeTags;
    }

    public JsonEntry inherit(JsonTypeTags parent, String entryMask) {
        JsonTypeTags typeTags;
        if (this.typeTags == null) {
            typeTags = parent;
        } else {
            typeTags = this.typeTags.inheritMissingTags(parent);
        }

        // apply mask
        String page = this.page;
        page = entryMask.replaceAll("<page>", page);
        page = page.replaceAll("<mod>", typeTags.mod);
        page = page.replaceAll("<type>", typeTags.type);
        page = page.replaceAll("<sub_mod>", typeTags.subMod);
        page = page.replaceAll("<sub_type>", typeTags.subType);
        return new JsonEntry(title, page, itemStack, typeTags);
    }

    public void printContents() {
        BCLog.logger.info("      title = " + title + ",");
        BCLog.logger.info("      page = " + page + ",");
        BCLog.logger.info("      item_stack = " + itemStack + ",");
        if (typeTags == null) {
            BCLog.logger.info("      type_tags = null");
        } else {
            BCLog.logger.info("      type_tags = {");
            typeTags.printContents(4);
            BCLog.logger.info("      }");
        }
    }
}
