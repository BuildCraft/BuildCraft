package buildcraft.lib.client.guide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.guide.parts.GuidePage;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.GuidePartFactory;

public class GuideExporter {
    public static void export(File folder) {
        List<GuideEntry> entries = new ArrayList<>();
        List<JsonPage> pages = new ArrayList<>();
        List<JsonInvalid> invalids = new ArrayList<>();

        GuiGuide guide = new GuiGuide();
        guide.initForExport();

        for (Entry<ResourceLocation, PageMeta> entry : GuideManager.pageMetas.entrySet()) {
            ResourceLocation loc = entry.getKey();
            PageMeta meta = entry.getValue();
            if (meta == null) {
                invalids.add(new JsonInvalid(loc.toString(), "No meta defined!"));
                continue;
            }
            GuidePartFactory<GuidePageBase> pageFactory = GuideManager.registeredPages.get(loc);
            GuidePage page = null;
            if (pageFactory != null) {
                GuidePageBase base = pageFactory.createNew(guide);
                if (base instanceof GuidePage) {
                    page = (GuidePage) base;
                }
            }
            if (page == null) {
                invalids.add(new JsonInvalid(loc.toString(), "No page defined!"));
                continue;
            }
            entries.add(new GuideEntry(meta, page));
            pages.add(new JsonPage(meta.title, "./"));// TODO: A proper layout
        }

    }

    public static class GuideEntry {
        public final PageMeta meta;
        public final GuidePage page;

        public GuideEntry(PageMeta meta, GuidePage page) {
            this.meta = meta;
            this.page = page;
        }
    }

    public static class JsonContents {
        public final JsonPage[] pages;
        public final JsonInvalid[] invalids;

        public JsonContents(JsonPage[] pages, JsonInvalid[] invalids) {
            this.pages = pages;
            this.invalids = invalids;
        }
    }

    public static class JsonPage {
        public final String title, location;

        public JsonPage(String title, String location) {
            this.title = title;
            this.location = location;
        }
    }

    public static class JsonInvalid {
        public final String location, error;

        public JsonInvalid(String location, String error) {
            this.location = location;
            this.error = error;
        }
    }
}
