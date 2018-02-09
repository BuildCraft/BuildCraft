/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.Nullable;

import net.minecraft.util.StringUtils;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.data.JsonEntry;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.loader.entry.PageEntryType;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.ColourUtil;

public class PageEntry<T> {

    public final String title, page;
    public final JsonTypeTags typeTags;
    public final PageEntryType<T> entryType;
    public final T value;

    private final List<String> searchTags = new ArrayList<>();

    @Nullable
    public static PageEntry<?> createPageEntry(JsonEntry entry) {
        String typeName = entry.type;
        if (typeName == null) {
            typeName = "";
        }

        PageEntryType<?> type = PageEntryType.REGISTRY.get(typeName);
        if (type == null) {
            String valids = new TreeSet<>(PageEntryType.REGISTRY.keySet()).toString();
            BCLog.logger.warn("[lib.guide] Unknown page entry type '" + typeName + "'. Valid ones are: " + valids);
            return null;
        }
        return createPageEntryKnown(entry, type);
    }

    @Nullable
    private static <T> PageEntry<T> createPageEntryKnown(JsonEntry entry, PageEntryType<T> type) {

        String src = entry.source;
        if (StringUtils.isNullOrEmpty(src)) {
            src = entry.itemStack;
            if (StringUtils.isNullOrEmpty(src)) {
                BCLog.logger.warn("[lib.guide] Invalid page entry: must specify either 'item_stack', or 'source'!");
                return null;
            }
        } else if (!StringUtils.isNullOrEmpty(entry.itemStack)) {
            BCLog.logger.warn(
                "[lib.guide] Invalid page entry: must only specify either 'item_stack' or 'source', but not both!");
            return null;
        }

        T value = type.deserialise(src);
        if (value == null) {
            BCLog.logger.warn("[lib.guide] Unknown source '" + src + "'");
            return null;
        }

        String title = entry.title;
        if (title == null || title.isEmpty()) {
            List<String> tooltip = type.getTooltip(value);
            title = ColourUtil.stripAllFormatCodes(tooltip.get(0));
        }
        if (StringUtils.isNullOrEmpty(entry.page)) {
            BCLog.logger.warn("[lib.guide] Invalid page entry: a page is not specified!SSS");
            return null;
        }
        return new PageEntry<T>(title, entry.page, entry.typeTags, type, value);
    }

    private PageEntry(String title, String page, JsonTypeTags typeTags, PageEntryType<T> entryType, T value) {
        this.title = title;
        this.page = page;
        this.typeTags = typeTags;
        this.entryType = entryType;
        this.value = value;

        searchTags.add(title);
        searchTags.add(typeTags.mod);
        searchTags.add(typeTags.subMod);
        searchTags.add(typeTags.type);
        searchTags.add(typeTags.subType);
        searchTags.addAll(entryType.getTooltip(value));
    }

    public List<String> getSearchTags() {
        return searchTags;
    }

    public boolean matches(Object obj) {
        return entryType.matches(value, obj);
    }

    public ISimpleDrawable createDrawable() {
        return entryType.createDrawable(value);
    }

    @Override
    public String toString() {
        return "PageEntry [title=" + title + ", page=" + page + "]";
    }
}
