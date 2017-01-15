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
import net.minecraft.item.ItemStack;

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

    public static class CreativeTabBC extends CreativeTabs {
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
        public ItemStack getTabIconItem() {
            return item;
        }
    }
}
