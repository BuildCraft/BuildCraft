package buildcraft.lib.client.guide;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.data.GuideEntryLoader;
import buildcraft.lib.client.guide.data.JsonEntry;
import buildcraft.lib.client.guide.loader.ILoadableResource;
import buildcraft.lib.client.guide.loader.IPageLoader;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePageStandInRecipes;

public enum GuideManager {
    INSTANCE;

    private static final String DEFAULT_LANG = "en_US";
    private static final Map<String, IPageLoader> PAGE_LOADERS = new HashMap<>();

    private final Map<PageEntry, ILoadableResource> entries = new HashMap<>();
    private final Map<String, GuidePageFactory> pages = new HashMap<>();
    private final Map<ItemStack, GuidePageFactory> generatedPages = new HashMap<>();

    static {
        PAGE_LOADERS.put("md", MarkdownPageLoader.INSTANCE);
    }

    public void load() {
        entries.clear();
        Map<JsonEntry, ILoadableResource> loaded = GuideEntryLoader.loadAll();
        for (Entry<JsonEntry, ILoadableResource> entry : loaded.entrySet()) {
            entries.put(new PageEntry(entry.getKey()), entry.getValue());
        }
        reloadLang();
    }

    public void reloadLang() {
        pages.clear();

        Language currentLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
        String langCode;
        if (currentLanguage == null) {
            BCLog.logger.warn("Current language was null!");
            langCode = DEFAULT_LANG;
        } else {
            langCode = currentLanguage.getLanguageCode();
        }

        // load the default ones
        loadLangInternal(DEFAULT_LANG);
        // replace any existing with the new ones.

        if (!DEFAULT_LANG.equals(langCode)) {
            loadLangInternal(langCode);
        }
    }

    private void loadLangInternal(String lang) {
        for (Entry<PageEntry, ILoadableResource> entry : entries.entrySet()) {
            PageEntry data = entry.getKey();
            ILoadableResource loadable = entry.getValue();
            String page = data.page.replaceAll("<lang>", lang);
            String ending = page.substring(page.lastIndexOf('.') + 1);

            IPageLoader loader = PAGE_LOADERS.get(ending);
            if (loader != null) {
                try (InputStream stream = loadable.getInputStreamFor(page)) {
                    if (stream == null) {
                        continue;
                    }
                    GuidePageFactory factory = loader.loadPage(stream, data);
                    // put the original page in so that the different lang variants override it
                    pages.put(data.page, factory);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }
    }

    public ImmutableSet<PageEntry> getAllEntries() {
        return ImmutableSet.copyOf(entries.keySet());
    }

    public GuidePageFactory getFactoryFor(PageEntry entry) {
        return pages.get(entry.page);
    }

    public PageEntry getEntryFor(@Nonnull ItemStack stack) {
        for (PageEntry entry : entries.keySet()) {
            if (entry.stackMatches(stack)) {
                return entry;
            }
        }
        return null;
    }

    @Nonnull
    public GuidePageFactory getPageFor(@Nonnull ItemStack stack) {
        PageEntry entry = getEntryFor(stack);
        if (entry != null) {
            GuidePageFactory factory = getFactoryFor(entry);
            if (factory != null) {
                return factory;
            }
        }
        // Create a dummy page for the stack
        GuidePageFactory existing = generatedPages.computeIfAbsent(stack, GuidePageStandInRecipes::createFactory);
        return existing;
    }
}
