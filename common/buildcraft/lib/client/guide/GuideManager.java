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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Stopwatch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.Language;
import net.minecraft.client.util.SuffixArray;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.api.statements.IStatement;

import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.entry.IEntryIterable;
import buildcraft.lib.client.guide.entry.IEntryLinkConsumer;
import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.loader.IPageLoader;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePageStandInRecipes;
import buildcraft.lib.client.guide.parts.contents.ContentsNode;
import buildcraft.lib.client.guide.parts.contents.ContentsNodeGui;
import buildcraft.lib.client.guide.parts.contents.GuidePageContents;
import buildcraft.lib.client.guide.parts.contents.IContentsNode;
import buildcraft.lib.client.guide.parts.contents.PageLink;
import buildcraft.lib.client.guide.parts.contents.PageLinkNormal;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.guide.GuideBook;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.guide.GuideContentsData;
import buildcraft.lib.misc.LocaleUtil;

public enum GuideManager implements IResourceManagerReloadListener {
    INSTANCE;

    public static final String DEFAULT_LANG = "en_us";
    public static final Map<String, IPageLoader> PAGE_LOADERS = new HashMap<>();
    public static final GuideContentsData BOOK_ALL_DATA = new GuideContentsData(null);

    private final List<PageEntry<?>> entries = new ArrayList<>();

    /** The keys are the partial paths, not the full ones!
     * <p>
     * For example a partial path might be "buildcraftcore:wrench.md" and the */
    private final Map<ResourceLocation, GuidePageFactory> pages = new HashMap<>();
    private final Map<ItemStack, GuidePageFactory> generatedPages = new HashMap<>();
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.guide.loader");

    /** Internal use only! Use {@link #addChild(ResourceLocation, JsonTypeTags, PageLink)} instead! */
    public SuffixArray<PageLink> quickSearcher;
    private final Map<GuideBook, Map<TypeOrder, ContentsNode>> contents = new HashMap<>();

    /** Every object added to the guide. Generally this means {@link Item}'s and {@link IStatement}'s. */
    public final Set<Object> objectsAdded = new HashSet<>();

    private boolean isInReload = false;

    static {
        PAGE_LOADERS.put("md", MarkdownPageLoader.INSTANCE);
    }

    public void onRegistryReload(EventBuildCraftReload.FinishLoad event) {
        if (isInReload) {
            // We reload the book registry while reloading this registry, so we don't need to reload it twice.
            // hang on... isn't this a bit hacky?
            // I feel like we shouldn't allow reloading everything by default?
            return;
        }
        if (event.reloadingRegistries.contains(GuideBookRegistry.INSTANCE)) {
            reload();
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        reload(resourceManager);
    }

    public void reload() {
        reload(Minecraft.getMinecraft().getResourceManager());
    }

    private void reload(IResourceManager resourceManager) {
        if (isInReload) {
            throw new IllegalStateException("Cannot reload while we are reloading!");
        }
        try {
            isInReload = true;
            reload0(resourceManager);
        } finally {
            isInReload = false;
        }
    }

    private void reload0(IResourceManager resourceManager) {
        Stopwatch watch = Stopwatch.createStarted();
        GuideBookRegistry.INSTANCE.reload();
        GuidePageRegistry.INSTANCE.reload();
        entries.clear();
        // Don't add permanent as we need the resource domain
        GuidePageRegistry manager = GuidePageRegistry.INSTANCE;
        Map<GuideBook, Set<String>> domains = new HashMap<>();
        domains.put(null, new HashSet<>());
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            domains.put(book, new HashSet<>());
        }

        for (PageEntry<?> entry : manager.getAllEntries()) {
            domains.get(null).add(entry.typeTags.domain);
            GuideBook book = GuideBookRegistry.INSTANCE.getBook(entry.book.toString());
            Set<String> domainSet = domains.get(book);
            if (domainSet != null && book != null) {
                domainSet.add(entry.typeTags.domain);
            }
            entries.add(entry);
        }
        BOOK_ALL_DATA.generate(domains.get(null));
        for (Entry<GuideBook, Set<String>> entry : domains.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            entry.getKey().data.generate(entry.getValue());
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

    private void loadLangInternal(IResourceManager resourceManager, String lang) {
        main_iteration: for (Entry<ResourceLocation, PageEntry> mapEntry : GuidePageRegistry.INSTANCE
            .getReloadableEntryMap().entrySet()) {
            ResourceLocation entryKey = mapEntry.getKey();
            String domain = entryKey.getResourceDomain();
            String path = "compat/buildcraft/guide/" + lang + "/" + entryKey.getResourcePath();

            for (Entry<String, IPageLoader> entry : PAGE_LOADERS.entrySet()) {
                ResourceLocation fLoc = new ResourceLocation(domain, path + "." + entry.getKey());

                try (InputStream stream = resourceManager.getResource(fLoc).getInputStream()) {
                    GuidePageFactory factory = entry.getValue().loadPage(stream, entryKey, mapEntry.getValue());
                    // put the original page in so that the different lang variants override it
                    pages.put(entryKey, factory);
                    if (GuideManager.DEBUG) {
                        BCLog.logger.info("[lib.guide.loader] Loaded page '" + entryKey + "'.");
                    }
                    continue main_iteration;
                } catch (FileNotFoundException f) {
                    // Ignore it, we'll log this later
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }

            if (pages.containsKey(entryKey)) {
                // We are overriding a different language so it's ok if we miss something.
                continue;
            }

            String endings;
            if (PAGE_LOADERS.size() == 1) {
                endings = PAGE_LOADERS.keySet().iterator().next();
            } else {
                endings = PAGE_LOADERS.keySet().toString();
            }
            BCLog.logger.warn(
                "[lib.guide.loader] Unable to load guide page '" + entryKey + "' (full path = '" + domain + ":" + path
                    + "." + endings + "') because we couldn't find any of the valid paths in any resource pack!");
        }
    }

    private void generateContentsPage() {
        objectsAdded.clear();
        contents.clear();
        contents.put(null, new HashMap<>());
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            Map<TypeOrder, ContentsNode> map = new HashMap<>();
            contents.put(book, map);
            for (TypeOrder order : GuiGuide.SORTING_TYPES) {
                map.put(order, new ContentsNode("root", -1));
            }
        }
        quickSearcher = new SuffixArray<>();

        for (Entry<ResourceLocation, PageEntry> mapEntry : GuidePageRegistry.INSTANCE.getReloadableEntryMap()
            .entrySet()) {
            ResourceLocation partialLocation = mapEntry.getKey();
            GuidePageFactory entryFactory = GuideManager.INSTANCE.getFactoryFor(partialLocation);

            PageEntry<?> entry = mapEntry.getValue();
            String translatedTitle = entry.title.getFormattedText();
            ISimpleDrawable icon = entry.createDrawable();
            PageLine line = new PageLine(icon, icon, 2, translatedTitle, true);

            if (entryFactory != null) {
                objectsAdded.add(entry.getBasicValue());
                PageLinkNormal pageLink = new PageLinkNormal(line, true, entry.getTooltip(), entryFactory);
                addChild(entry.book, entry.typeTags, pageLink);
            }
        }

        final IEntryLinkConsumer adder = (tags, page) -> addChild(null, tags, page);
        for (IEntryIterable type : GuidePageRegistry.ENTRY_ITERABLES) {
            type.iterateAllDefault(adder);
        }

        quickSearcher.generate();
        for (Map<TypeOrder, ContentsNode> map : contents.values()) {
            for (ContentsNode node : map.values()) {
                node.sort();
            }
        }
    }

    private void addChild(ResourceLocation bookType, JsonTypeTags tags, PageLink page) {
        for (Entry<GuideBook, Map<TypeOrder, ContentsNode>> bookEntry : contents.entrySet()) {

            @Nullable
            GuideBook book = bookEntry.getKey();
            if (bookType == null) {
                if (book != null && !book.appendAllEntries) {
                    continue;
                }
            } else if (book != null && !book.name.equals(bookType)) {
                continue;
            }
            Map<TypeOrder, ContentsNode> map = bookEntry.getValue();
            for (Entry<TypeOrder, ContentsNode> entry : map.entrySet()) {
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

    }

    public GuidePageFactory getFactoryFor(ResourceLocation partialLocation) {
        return pages.get(partialLocation);
    }

    public static ResourceLocation getEntryFor(Object obj) {
        for (Entry<ResourceLocation, PageEntry> entry : GuidePageRegistry.INSTANCE.getReloadableEntryMap().entrySet()) {
            if (entry.getValue().matches(obj)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Nonnull
    public GuidePageFactory getPageFor(@Nonnull ItemStack stack) {
        ResourceLocation entry = getEntryFor(stack);
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
        Map<TypeOrder, ContentsNode> map = contents.get(gui.book);
        if (map == null) {
            throw new IllegalStateException("Unknown book " + gui.book);
        }
        ContentsNode node = map.get(sortingOrder);
        if (node == null) {
            throw new IllegalStateException("Unknown sorting order " + sortingOrder);
        }
        node.resetVisibility();

        return new ContentsNodeGui(gui, guidePageContents, node);
    }
}
