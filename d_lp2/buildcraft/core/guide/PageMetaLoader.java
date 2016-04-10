package buildcraft.core.guide;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

public class PageMetaLoader extends LocationLoader {
    public static PageMeta load(ResourceLocation location) {
        String text = asString(location);
        if (text.length() == 0) {
            return new PageMeta(location.toString(), "", "");
        }
        try {
            return new Gson().fromJson(text, PageMeta.class);
        } catch (JsonSyntaxException ex) {
            BCLog.logger.warn("Could not load the resource location " + location + " because an exception was thrown!", ex);
            return new PageMeta(location.toString(), "", "");
        }
    }
}
