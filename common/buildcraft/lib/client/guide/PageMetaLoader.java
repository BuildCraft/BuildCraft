package buildcraft.lib.client.guide;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.PageMeta.EStage;
import buildcraft.lib.client.guide.PageMeta.PageTypeTags;

public class PageMetaLoader extends LocationLoader {
    private static final PageTypeTags UNKNOWN_TAGS = new PageTypeTags("unknown", "", EStage.BASE, "unknown", "");

    public static PageMeta load(ResourceLocation location) {
        String text = asString(location);
        if (text.length() == 0) {
            return new PageMeta(location.toString(), "", UNKNOWN_TAGS);
        }
        try {
            return new Gson().fromJson(text, PageMeta.class);
        } catch (JsonSyntaxException ex) {
            BCLog.logger.warn("[guide-page-meta] Could not load the resource location " + location + " because an exception was thrown!", ex);
            return new PageMeta(location.toString(), "", UNKNOWN_TAGS);
        }
    }
}
