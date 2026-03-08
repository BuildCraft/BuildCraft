/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;


import buildcraft.api.transport.pipe.IItemPipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CreativeTabManager {
    // Calen: thread safety, avoid creating duplicate tabs
//    private static final Map<String, CreativeTabBC> tabMap = new HashMap<>();
    private static final Map<String, CreativeTabBC> tabMap = new ConcurrentHashMap<>();

    public static ItemGroup getTab(String name) {
        if (name.startsWith("vanilla.")) {
            String after = name.substring("vanilla.".length());
            switch (after) {
                case "misc":
                    return ItemGroup.TAB_MISC;
                case "materials":
                    return ItemGroup.TAB_MATERIALS;
            }
        }
        if (tabMap.containsKey(name)) {
            return tabMap.get(name);
        } else {
            throw new IllegalArgumentException("Unknown tab " + name);
        }
    }

    // public static CreativeTabBC createTab(String name)
    public static synchronized CreativeTabBC createTab(String name) {
        CreativeTabBC tab = tabMap.get(name);
        if (tab != null) {
            return tab;
        }
        tab = new CreativeTabBC(name);
        tabMap.put(name, tab);
        return tab;
    }

    // public static void setItem(String name, Item item)
    public static void setItem(String name, Supplier<? extends Item> item) {
        if (item != null) {
//            setItemStack(name, new ItemStack(item));
            setItemStack(name, () -> new ItemStack(item.get()));
        }
    }

    // public static void setItemStack(String name, ItemStack item)
    public static void setItemStack(String name, Supplier<ItemStack> item) {
        CreativeTabBC tab = tabMap.get(name);
        if (tab != null) {
//            tab.setItem(item);
            tab.setItemStack(item);
        }
    }

    public static class CreativeTabBC extends ItemGroup {
        // private ItemStack item = new ItemStack(Items.COMPARATOR); // Temp.
        private Supplier<ItemStack> iconItem = () -> new ItemStack(Items.COMPARATOR); // Temp.

        // private CreativeTabBC(String name)
        private CreativeTabBC(String name) {
            super(name);
        }


        // public void setItem(Item item)
        public void setItem(Supplier<? extends Item> item) {
            if (item != null) {
//                this.item = new ItemStack(item);
                this.iconItem = () -> new ItemStack(item.get());
            }
        }

        public void setItemPipe(Supplier<? extends IItemPipe> item) {
            if (item != null) {
//                this.item = new ItemStack(item);
                this.iconItem = () -> new ItemStack((Item) item.get());
            }
        }

        // public void setItem(ItemStack stack)
        public void setItemStack(Supplier<ItemStack> stack) {
//            if (stack == null || stack.isEmpty()) return;
//            item = stack;
            this.iconItem = stack;
        }

        @Override
//        public ItemStack getTabIconItem()
        public ItemStack makeIcon() {
//            return item;
            return iconItem.get();
        }
    }
}
