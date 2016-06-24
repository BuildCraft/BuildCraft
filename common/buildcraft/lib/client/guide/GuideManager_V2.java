package buildcraft.lib.client.guide;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;

import buildcraft.lib.client.guide.data.GuideEntryLoader;
import buildcraft.lib.client.guide.data.JsonEntry;
import buildcraft.lib.client.guide.loader.ILoadableResource;
import buildcraft.lib.client.guide.loader.IPageLoader;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.GuidePartFactory;

public enum GuideManager_V2 {
    INSTANCE;

    private static final String DEFAULT_LANG = "en_US";
    private static final Map<String, IPageLoader> PAGE_LOADERS = new HashMap<>();

    private final Map<JsonEntry, ILoadableResource> entries = new HashMap<>();
    private final Map<String, GuidePartFactory<? extends GuidePageBase>> pages = new HashMap<>();

    static {
        PAGE_LOADERS.put("md", MarkdownPageLoader.INSTANCE);
    }

    public void load() {
        entries.clear();
        entries.putAll(GuideEntryLoader.loadAll());
        reloadLang();
    }

    // TODO:
    // - contents page
    // - loading from item stacks
    // -

    public void reloadLang() {
        pages.clear();

        String lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();

        // load the default ones
        loadLangInternal(DEFAULT_LANG);
        // replace any existing with the new ones.

        if (!DEFAULT_LANG.equals(lang)) {
            loadLangInternal(lang);
        }
    }

    private void loadLangInternal(String lang) {
        for (Entry<JsonEntry, ILoadableResource> entry : entries.entrySet()) {
            JsonEntry data = entry.getKey();
            ILoadableResource loadable = entry.getValue();
            String page = data.page.replaceAll("<lang>", lang);
            String ending = page.substring(page.lastIndexOf('.') + 1);

            IPageLoader loader = PAGE_LOADERS.get(ending);
            if (loader != null) {
                try (InputStream stream = loadable.getInputStreamFor(page)) {
                    if (stream == null) {
                        continue;
                    }
                    GuidePartFactory<? extends GuidePageBase> factory = loader.loadPage(stream, data);
                    // put the original page in so that the different lang variants override it
                    pages.put(data.page, factory);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }
    }
}
