/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class CreativeTabManager {
    private static final Map<String, CreativeTabBC> tabMap = new HashMap<>();

    public static CreativeTabs getTab(String name) {
        if (name.startsWith("vanilla.")) {
            String after = name.substring("vanilla.".length());
            switch (after) {
                case "misc":
                    return CreativeTabs.MISC;
                case "materials":
                    return CreativeTabs.MATERIALS;
            }
        }
        if (tabMap.containsKey(name)) {
            return tabMap.get(name);
        } else {
            throw new IllegalArgumentException("Unknown tab " + name);
        }
    }

    public static void createTab(String name) {
        tabMap.put(name, new CreativeTabBC(name));
    }

    public static void setItem(String name, Item item) {
        if (item != null) {
            tabMap.get(name).item = item;
        }
    }

    private static class CreativeTabBC extends CreativeTabs {
        private Item item = Items.COMPARATOR; // Temp.

        public CreativeTabBC(String name) {
            super(name);
        }

        @Override
        public Item getTabIconItem() {
            return item;
        }
    }
}
