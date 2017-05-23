/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import net.minecraft.item.ItemFood;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.lists.ListRegistry;

public class VanillaListHandlers {
    public static void fmlInit() {
        ListRegistry.registerHandler(new ListMatchHandlerClass());
        ListRegistry.registerHandler(new ListMatchHandlerFluid());
        ListRegistry.registerHandler(new ListMatchHandlerTools());
        ListRegistry.registerHandler(new ListMatchHandlerArmor());
        ListRegistry.itemClassAsType.add(ItemFood.class);
    }

    public static void fmlPostInit() {
        for (String s : OreDictionary.getOreNames()) {
            ListOreDictionaryCache.INSTANCE.registerName(s);
        }
        ListRegistry.registerHandler(new ListMatchHandlerOreDictionary());
    }
}
