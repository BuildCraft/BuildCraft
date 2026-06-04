/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): ListHandler list-matching logic deferred
package buildcraft.lib.list;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler;

public final class ListHandler {

    public static boolean matches(ItemStack filter, @Nonnull ItemStack stack,
            ListMatchHandler.Type type, boolean whiteList) {
        return false;
    }

    public enum TypeOrder {
        WHITELIST, BLACKLIST;
    }
}
