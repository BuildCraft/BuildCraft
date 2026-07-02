/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.lib;

import java.util.HashMap;
import java.util.Map;


import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CreativeTabManager {
    private static final Map<String, CreativeTabBC> tabMap = new HashMap<>();

    public static CreativeModeTab getTab(String name) {
        if (name.startsWith("vanilla.")) {
            String after = name.substring("vanilla.".length());
            switch (after) {
                case "misc":
                    return CreativeModeTab.TAB_MISC;
                case "materials":
                    return CreativeModeTab.TAB_MATERIALS;
            }
        }
        if (tabMap.containsKey(name)) {
            return tabMap.get(name);
        } else {
            throw new IllegalArgumentException("Unknown tab " + name);
        }
    }

    public static CreativeTabBC createTab(String name) {
        CreativeTabBC tab = tabMap.get(name);
        if (tab != null) {
            return tab;
        }
        tab = new CreativeTabBC(name);
        tabMap.put(name, tab);
        return tab;
    }

    public static void setItem(String name, Item item) {
        if (item != null) {
            setItemStack(name, new ItemStack(item));
        }
    }

    public static void setItemStack(String name, ItemStack item) {
        CreativeTabBC tab = tabMap.get(name);
        if (tab != null) {
            tab.setItem(item);
        }
    }

    public static class CreativeTabBC extends CreativeModeTab {
        private ItemStack item = new ItemStack(Items.COMPARATOR); // Temp.

        private CreativeTabBC(String name) {
            super(name);
        }

        public void setItem(Item item) {
            if (item != null) {
                this.item = new ItemStack(item);
            }
        }

        public void setItem(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return;
            item = stack;
        }

        @Override
        public ItemStack makeIcon() {
            return item;
        }
    }
}
