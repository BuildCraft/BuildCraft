/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): guide item-stack page entry deferred
package buildcraft.lib.client.guide.entry;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import buildcraft.api.registry.IScriptableRegistry.OptionallyDisabled;

@Environment(EnvType.CLIENT)
public class PageEntryItemStack extends PageValueType<ItemStackValueFilter> {

    public static final PageEntryItemStack INSTANCE = new PageEntryItemStack();

    @Override
    public OptionallyDisabled<PageEntry<ItemStackValueFilter>> deserialize(Identifier name, JsonObject json,
            JsonDeserializationContext ctx) {
        return OptionallyDisabled.present(new PageEntry<>(name, new ItemStackValueFilter(ItemStack.EMPTY), this));
    }
}
