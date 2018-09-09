/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.Language;
import net.minecraft.client.util.SuffixArray;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.statements.IStatement;

import buildcraft.lib.client.guide.data.JsonContents;
import buildcraft.lib.client.guide.data.JsonEntry;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.loader.IPageLoader;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.loader.entry.IEntryLinkConsumer;
import buildcraft.lib.client.guide.loader.entry.PageEntryType;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePageStandInRecipes;
import buildcraft.lib.client.guide.parts.contents.ContentsNode;
import buildcraft.lib.client.guide.parts.contents.ContentsNodeGui;
import buildcraft.lib.client.guide.parts.contents.GuidePageContents;
import buildcraft.lib.client.guide.parts.contents.IContentsNode;
import buildcraft.lib.client.guide.parts.contents.PageLink;
import buildcraft.lib.client.guide.parts.contents.PageLinkNormal;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.LocaleUtil;

public enum GuideManager implements IResourceManagerReloadListener {
    INSTANCE;

    public static final List<String> loadedDomains = new ArrayList<>();
    public static final List<String> loadedMods = new ArrayList<>();
    public static final List<String> loadedOther = new ArrayList<>();
    public static final Set<BCModules> buildCraftModules = EnumSet.noneOf(BCModules.class);

    public static final String DEFAULT_LANG = "en_us";
    public static final Map<String, IPageLoader> PAGE_LOADERS = new HashMap<>();

    private final List<PageEntry<?>> entries = new ArrayList<>();
    private final Map<String, GuidePageFactory> pages = new HashMap<>();
    private final Map<ItemStack, GuidePageFactory> generatedPages = new HashMap<>();
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.guide.loader");

    /** Internal use only! Use {@link #addChild(JsonTypeTags, PageLink)} instead! */
    public SuffixArray<PageLink> quickSearcher;
    private final Map<TypeOrder, ContentsNode> contents = new HashMap<>();

    /** Every object added to the guide. Generally this means {@link Item}'s and {@link IStatement}'s. */
    public final Set<Object> objectsAdded = new HashSet<>();

    static {
        PAGE_LOADERS.put("md", MarkdownPageLoader.INSTANCE);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        reload(resourceManager);
    }

    public void reload() {
        reload(Minecraft.getMinecraft().getResourceManager());
    }

    private void reload(IResourceManager resourceManager) {
        Stopwatch watch = Stopwatch.createStarted();
        entries.clear();
        loadedDomains.clear();
        loadedMods.clear();
        loadedOther.clear();
        List<JsonEntry> loaded = loadAll(resourceManager);
        for (JsonEntry entry : loaded) {
            PageEntry<?> pageEntry = PageEntry.createPageEntry(entry);
            if (pageEntry == null) {
                continue;
            }
            entries.add(pageEntry);
        }
        Collections.sort(loadedDomains);
        // Special sort -- replace mod domains with mod names

        buildCraftModules.clear();
        for (BCModules module : BCModules.VALUES) {
            if (loadedDomains.remove(module.getModId())) {
                buildCraftModules.add(module);
            }
        }

        int moduleCount = buildCraftModules.size();
        int maxModuleCount = BCModules.VALUES.length;
        if (moduleCount == maxModuleCount) {
            loadedMods.add("BuildCraft (+Compat)");
        } else if (moduleCount == maxModuleCount - 1 && !buildCraftModules.contains(BCModules.COMPAT)) {
            loadedMods.add("BuildCraft");
        } else if (moduleCount > 2) {
            loadedMods.add("BuildCraft (§o" + (moduleCount - 2) + " modules§r)");
        } else if (moduleCount == 2) {
            loadedMods.add("BuildCraft (Core)");
        } else {
            loadedMods.add("BuildCraft (Lib)");
        }

        Iterator<String> domainIter = loadedDomains.iterator();
        String domain;
        while (domainIter.hasNext()) {
            domain = domainIter.next();
            ModContainer mod = Loader.instance().getIndexedModList().get(domain);
            if (mod != null) {
                loadedMods.add(mod.getName());
            } else {
                loadedOther.add(LocaleUtil.localize(domain + ".compat.buildcraft.guide.domain_name"));
            }
        }

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
        loadLangInternal(resourceManager, DEFAULT_LANG);
        // replace any existing with the new ones.

        if (!DEFAULT_LANG.equals(langCode)) {
            loadLangInternal(resourceManager, langCode);
        }

        generateContentsPage();

        watch.stop();
        long time = watch.elapsed(TimeUnit.MILLISECONDS);
        int p = entries.size();
        int a = pages.size();
        int e = p - a;
        BCLog.logger.info("[lib.guide] Loaded " + p + " possible and " + a + " actual guide pages (" + e
            + " not found) in " + time + "ms.");
    }

    private static List<JsonEntry> loadAll(IResourceManager resourceManager) {
        List<JsonEntry> allEntries = new ArrayList<>();

        for (String domain : resourceManager.getResourceDomains()) {

            JsonContents contents = loadContents(resourceManager, domain);

            if (contents != null) {
                GuideManager.loadedDomains.add(domain);
                contents = contents.inheritMissingTags();
                for (JsonEntry entry : contents.contents) {
                    allEntries.add(entry);
                }
            }
        }
        return allEntries;
    }

    private static JsonContents loadContents(IResourceManager resourceManager, String domain) {
        ResourceLocation location = new ResourceLocation(domain, "compat/buildcraft/guide/contents.json");

        try (InputStream is = resourceManager.getResource(location).getInputStream()) {
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                return new Gson().fromJson(isr, JsonContents.class);
            }
            return null;
        } catch (FileNotFoundException fnfe) {
            if (GuideManager.DEBUG) {
                BCLog.logger.warn(
                    "[lib.guide.loader] Looks like there is no guide contents page for " + location + ", skipping.");
            }
            return null;
        } catch (IOException io) {
            if (GuideManager.DEBUG) {
                BCLog.logger.warn("[lib.guide.loader] Failed to load the contents file for " + domain + "!", io);
            } else {
                BCLog.logger
                    .warn("[lib.guide.loader] Failed to load the contents file for " + domain + ": " + io.getMessage());
            }
            return null;
        }
    }

    private void loadLangInternal(IResourceManager resourceManager, String lang) {
        for (PageEntry<?> data : entries) {
            String page = data.page.replaceAll("<lang>", lang);
            String ending = page.substring(page.lastIndexOf('.') + 1);

            IPageLoader loader = PAGE_LOADERS.get(ending);
            if (loader == null) {
                BCLog.logger.warn("[lib.guide.loader] Unable to load guide page '" + page
                    + "', as we don't know how to load it! (Known file type endings are " + PAGE_LOADERS.keySet()
                    + ")");
                continue;
            }
            try (InputStream stream = resourceManager.getResource(new ResourceLocation(page)).getInputStream()) {
                GuidePageFactory factory = loader.loadPage(stream, data);
                // put the original page in so that the different lang variants override it
                pages.put(data.page, factory);
                if (GuideManager.DEBUG) {
                    BCLog.logger.info("[lib.guide.loader] Loaded page '" + page + "'.");
                }
            } catch (FileNotFoundException fnfe) {
                BCLog.logger.warn("[lib.guide.loader] Unable to load guide page '" + page
                    + "' because we couldn't find it in any resource pack!");
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    private void generateContentsPage() {
        objectsAdded.clear();
        contents.clear();
        for (TypeOrder order : GuiGuide.SORTING_TYPES) {
            contents.put(order, new ContentsNode("root", -1));
        }
        quickSearcher = new SuffixArray<>();

        for (PageEntry<?> entry : GuideManager.INSTANCE.getAllEntries()) {
            GuidePageFactory entryFactory = GuideManager.INSTANCE.getFactoryFor(entry);

            String translatedTitle = LocaleUtil.localize(entry.title);
            ISimpleDrawable icon = entry.createDrawable();
            PageLine line = new PageLine(icon, icon, 2, translatedTitle, true);

            if (entryFactory != null) {
                objectsAdded.add(entry.getBasicValue());
                PageLinkNormal pageLink = new PageLinkNormal(line, true, entry.getTooltip(), entryFactory);
                addChild(entry.typeTags, pageLink);
            }
        }

        final IEntryLinkConsumer adder = this::addChild;
        for (PageEntryType<?> type : new HashSet<>(PageEntryType.REGISTRY.values())) {
            type.iterateAllDefault(adder);
        }

        quickSearcher.generate();
        for (ContentsNode node : contents.values()) {
            node.sort();
        }
    }

    private void addChild(JsonTypeTags tags, PageLink page) {
        for (Entry<TypeOrder, ContentsNode> entry : contents.entrySet()) {
            TypeOrder order = entry.getKey();
            ContentsNode node = entry.getValue();

            String[] ordered = tags.getOrdered(order);
            for (int i = 0; i < ordered.length; i++) {
                String title = LocaleUtil.localize(ordered[i]);
                IContentsNode subNode = node.getChild(title);
                if (subNode instanceof ContentsNode) {
                    node = (ContentsNode) subNode;
                } else if (subNode == null) {
                    ContentsNode subContents = new ContentsNode(title, i);
                    node.addChild(subContents);
                    node = subContents;
                } else {
                    throw new IllegalStateException("Unknown node type " + subNode.getClass());
                }
            }
            node.addChild(page);
            quickSearcher.add(page, page.getSearchName());
        }
    }

    public ImmutableList<PageEntry<?>> getAllEntries() {
        return ImmutableList.copyOf(entries);
    }

    public GuidePageFactory getFactoryFor(PageEntry<?> entry) {
        return pages.get(entry.page);
    }

    public PageEntry<?> getEntryFor(Object obj) {
        for (PageEntry<?> entry : entries) {
            if (entry.matches(obj)) {
                return entry;
            }
        }
        return null;
    }

    @Nonnull
    public GuidePageFactory getPageFor(@Nonnull ItemStack stack) {
        PageEntry<?> entry = getEntryFor(stack);
        if (entry != null) {
            GuidePageFactory factory = getFactoryFor(entry);
            if (factory != null) {
                return factory;
            }
        }
        // Create a dummy page for the stack
        return generatedPages.computeIfAbsent(stack, GuidePageStandInRecipes::createFactory);
    }

    public ContentsNodeGui getGuiContents(GuiGuide gui, GuidePageContents guidePageContents, TypeOrder sortingOrder) {

        ContentsNode node = contents.get(sortingOrder);

        if (node == null) {
            throw new IllegalStateException("Unknown sorting order " + sortingOrder);
        }
        node.resetVisibility();

        return new ContentsNodeGui(gui, guidePageContents, node);
    }
}
