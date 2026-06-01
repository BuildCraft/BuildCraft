package buildcraft.lib.client.guide.entry;

import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;

import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.misc.JsonUtil;

public final class PageEntry<T> extends PageValue<T> {

    public final JsonTypeTags typeTags;
    public final Identifier book;

    public PageEntry(PageValueType<T> type, JsonTypeTags typeTags, Identifier book, T value) {
        super(type, value);
        this.typeTags = typeTags;
        this.book = book;
    }

    public PageEntry(PageValueType<T> type, Identifier name, JsonObject json, T value) {
        super(type, value);
        this.book = JsonUtil.getIdentifier(json, "book");
        String tagType = json.get("tag_type").getAsString();
        String subType = json.get("tag_subtype").getAsString();
        this.typeTags = new JsonTypeTags(name.getNamespace(), tagType, subType);
    }

    @Override
    public String toString() {
        return value.getClass().getSimpleName() + ": " + value;
    }
}
