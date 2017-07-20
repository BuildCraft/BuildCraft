/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.data.JsonEntry;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;

public class PageEntry {
    public final String title, page;
    public final JsonTypeTags typeTags;
    private final ItemStack stack;
    private final boolean containsMeta, containsNbt;

    public PageEntry(JsonEntry entry) {
        this.title = entry.title;
        this.page = entry.page;
        this.typeTags = entry.typeTags;
        // parse item stack
        if (StringUtils.isNullOrEmpty(entry.itemStack)) {
            stack = null;
            containsMeta = false;
            containsNbt = false;
        } else if (entry.itemStack.startsWith("(") && entry.itemStack.endsWith(")")) {
            String inner = entry.itemStack.substring(1, entry.itemStack.length() - 1);
            Item item = Item.getByNameOrId(inner);
            if (item != null) {
                stack = new ItemStack(item);
            } else {
                BCLog.logger.warn("[lib.markdown] " + inner + " was not a valid item!");
                stack = null;
            }
            containsMeta = false;
            containsNbt = false;
        } else if (entry.itemStack.startsWith("{") && entry.itemStack.endsWith("}")) {
            String inner = entry.itemStack.substring(1, entry.itemStack.length() - 1);
            String[] split = inner.split(",");
            stack = MarkdownPageLoader.loadComplexItemStack(inner);
            containsMeta = split.length >= 3;
            containsNbt = split.length >= 4;
        } else {
            // print warning
            stack = null;
            containsMeta = false;
            containsNbt = false;
        }
    }

    public boolean stackMatches(ItemStack test) {
        if (stack == null || test == null) {
            return false;
        }
        if (stack.getItem() != test.getItem()) {
            return false;
        }
        if (containsMeta) {
            if (stack.getMetadata() != test.getMetadata()) {
                return false;
            }
        }
        if (containsNbt) {
            if (!ItemStack.areItemStackTagsEqual(stack, test)) {
                return false;
            }
        }

        return true;
    }

    public ItemStack getItemStack() {
        return stack == null ? null : stack.copy();
    }

    @Override
    public String toString() {
        return "PageEntry [title=" + title + ", page=" + page + "]";
    }
}
