/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import net.minecraft.item.FoodItem;
import buildcraft.lib.compat.forge_stubs.OreDictionaryStub;

// STUB(R.Chen): OreDictionaryStub removed — TODO(R.Chen): implement via Tags

import buildcraft.api.lists.ListRegistry;

public class VanillaListHandlers {
    public static void fmlInit() {
        ListRegistry.registerHandler(new ListMatchHandlerClass());
        ListRegistry.registerHandler(new ListMatchHandlerFluid());
        ListRegistry.registerHandler(new ListMatchHandlerTools());
        ListRegistry.registerHandler(new ListMatchHandlerArmor());
        ListRegistry.itemClassAsType.add(FoodItem.class);
    }

    public static void fmlPostInit() {
        for (String s : OreDictionaryStub.getOreNames()) {
            ListOreDictionaryCache.INSTANCE.registerName(s);
        }
        ListRegistry.registerHandler(new ListMatchHandlerOreDictionary());
    }
}
