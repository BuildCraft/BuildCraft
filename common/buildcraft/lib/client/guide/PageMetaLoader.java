package buildcraft.lib.client.guide;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.guide.PageMeta.PageTypeTags;

@Deprecated
public class PageMetaLoader extends LocationLoader {
    private static final PageTypeTags UNKNOWN_TAGS = new PageTypeTags("unknown", ""/* , EStage.BASE */, "unknown", "");

    public static PageMeta load(ResourceLocation location) {
        // String text = asString(location);
        // if (text.length() == 0) {
        // return new PageMeta(location.toString(), "", UNKNOWN_TAGS);
        // }
        // try {
        // return new Gson().fromJson(text, PageMeta.class);
        // } catch (JsonSyntaxException ex) {
        // BCLog.logger.warn("[guide-page-meta] Could not load the resource location " + location + " because an
        // exception was thrown!", ex);
        // return new PageMeta(location.toString(), "", UNKNOWN_TAGS);
        // }
        throw new IllegalStateException("lol");
    }
}
