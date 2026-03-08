package buildcraft.lib.client.guide.entry;

import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public final class PageEntry<T> extends PageValue<T> {

    public final JsonTypeTags typeTags;
    public final ResourceLocation book;

    public PageEntry(PageValueType<T> type, JsonTypeTags typeTags, ResourceLocation book, T value) {
        super(type, value);
        this.typeTags = typeTags;
        this.book = book;
    }

    public PageEntry(PageValueType<T> type, ResourceLocation name, JsonObject json, T value) {
        super(type, value);
        this.book = JsonUtil.getIdentifier(json, "book");
//        String tagType = JsonUtils.getString(json, "tag_type");
        String tagType = JSONUtils.getAsString(json, "tag_type");
//        String subType = JsonUtils.getString(json, "tag_subtype");
        String subType = JSONUtils.getAsString(json, "tag_subtype");
        this.typeTags = new JsonTypeTags(name.getNamespace(), tagType, subType);
    }

    @Override
    public String toString() {
        return value.getClass().getSimpleName() + ": " + value;
    }
}
