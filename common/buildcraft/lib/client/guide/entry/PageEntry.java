/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.entry;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.JsonUtil;

public abstract class PageEntry<T> {
    public final JsonTypeTags typeTags;
    public final ResourceLocation book;
    public final ITextComponent title;

    public final T value;

    public PageEntry(ResourceLocation name, JsonObject json, T value, JsonDeserializationContext ctx)
        throws JsonParseException {
        this(name, json, value, JsonUtil.getTextComponent(json, "title", "buildcraft.guide.page."), ctx);
    }

    public PageEntry(ResourceLocation name, JsonObject json, T value, ITextComponent title,
        JsonDeserializationContext ctx) throws JsonParseException {
        this.title = title;
        this.book = JsonUtil.getIdentifier(json, "book");
        this.value = value;
        String type = JsonUtils.getString(json, "tag_type");
        String subType = JsonUtils.getString(json, "tag_subtype");
        this.typeTags = new JsonTypeTags(name.getResourceDomain(), type, subType);
    }

    public PageEntry(JsonTypeTags typeTags, ResourceLocation book, ITextComponent title, T value) {
        this.typeTags = typeTags;
        this.book = book;
        this.title = title;
        this.value = value;
    }

    /** @param test An unknown object.
     * @return True if it matches {@link #value} */
    public boolean matches(Object test) {
        return Objects.equals(test, value);
    }

    @Nullable
    public abstract ISimpleDrawable createDrawable();

    /** @return A value to be added to {@link GuideManager#objectsAdded} so that
     *         {@link IEntryIterable#iterateAllDefault(IEntryLinkConsumer)} can ignore similar entries. */
    public Object getBasicValue() {
        return value;
    }

    public abstract List<String> getTooltip();
}
