/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class VanillaListHandlers {
    public static void fmlInit() {
        ListRegistry.registerHandler(new ListMatchHandlerClass());
        ListRegistry.registerHandler(new ListMatchHandlerFluid());
        ListRegistry.registerHandler(new ListMatchHandlerTools());
        ListRegistry.registerHandler(new ListMatchHandlerArmor());
//        ListRegistry.itemClassAsType.add(Foods.class);
        ListRegistry.registerHandler(new ListMatchHandlerFood());
    }

    public static void fmlPostInit() {
//        for (String s : OreDictionary.getOreNames())
        for (TagKey<Item> s : ForgeRegistries.ITEMS.tags().getTagNames().toList()) {
            ListOreDictionaryCache.INSTANCE.registerName(s);
        }
        ListRegistry.registerHandler(new ListMatchHandlerOreDictionary());
    }
}
