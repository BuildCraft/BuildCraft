/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;


import buildcraft.api.BCModules;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CreativeTabManager {
    // Calen: thread safety, avoid creating duplicate tabs
    // private static final Map<String, CreativeTabBC> tabMap = new HashMap<>();
    public static final Map<String, CreativeTabBC> tabMap = new ConcurrentHashMap<>();
    private static final Map<String, Supplier<ItemStack>> tabIconMap = new ConcurrentHashMap<>();
    private static final Map<CreativeModeTab, NonNullList<Item>> bcTabItemsMap = new ConcurrentHashMap<>();
    private static final Map<CreativeModeTab, NonNullList<Item>> vanillaTabItemsMap = new ConcurrentHashMap<>();

    public static void addItemsToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        NonNullList<Item> items = vanillaTabItemsMap.get(event.getTab());
        if (items != null) {
            items.forEach(item -> {
                if (item instanceof IItemBuildCraft) {
                    NonNullList<ItemStack> stacks = NonNullList.create();
                    ((IItemBuildCraft) item).fillItemCategory(stacks);
                    event.acceptAll(stacks);
                } else {
                    event.accept(new ItemStack(item));
                }
            });
        }
    }

    public static CreativeModeTab getTab(String name) {
        if (name.startsWith("vanilla.")) {
            String after = name.substring("vanilla.".length());
            CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation.tryParse(after));
            if (tab != null) {
                return tab;
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

    public static void addItem(CreativeModeTab tab, Item item) {
        NonNullList<Item> bcTabItems = bcTabItemsMap.get(tab);
        if (bcTabItems != null) {
            // bc tab
            bcTabItemsMap.get(tab).add(item);
        } else {
            // vanilla tab
            NonNullList<Item> items = vanillaTabItemsMap.computeIfAbsent(tab, (t) -> NonNullList.create());
            items.add(item);
        }
    }

    private static void buildDisplayItems(String name, CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
        NonNullList<Item> items = bcTabItemsMap.get(getTab(name));
        if (items != null) {
            for (Item item : items) {
                if (item instanceof IItemBuildCraft itemBuildCraft) {
                    NonNullList<ItemStack> itemStacks = NonNullList.create();
                    itemBuildCraft.fillItemCategory(itemStacks);
                    output.acceptAll(itemStacks);
                } else {
                    output.accept(new ItemStack(item));
                }
            }
        }
    }

    public static class CreativeTabBC extends CreativeModeTab {
        // private ItemStack item = new ItemStack(Items.COMPARATOR); // Temp.
        private static final Supplier<ItemStack> tempIconItem = () -> new ItemStack(Items.COMPARATOR); // Temp.
        private ItemStack stack = StackUtil.EMPTY;
        private final String name;

        // private CreativeTabBC(String name)
        private CreativeTabBC(String name) {
            super(CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + name))
                    .icon(() -> CreativeTabManager.tabIconMap.get(name).get())
                    .displayItems((p, o) -> CreativeTabManager.buildDisplayItems(name, p, o))
            );
            this.name = name;
            CreativeTabManager.tabIconMap.put(name, tempIconItem);
            CreativeTabManager.bcTabItemsMap.put(this, NonNullList.create());
        }

        // public void setItem(Item item)
        public void setItem(Supplier<? extends Item> item) {
            if (item != null) {
//                this.item = new ItemStack(item);
                CreativeTabManager.tabIconMap.put(this.name, () -> new ItemStack(item.get()));
            }
        }

        public void setItemPipe(Supplier<? extends IItemPipe> item) {
            if (item != null) {
//                this.item = new ItemStack(item);
                CreativeTabManager.tabIconMap.put(this.name, () -> new ItemStack((Item) item.get()));
            }
        }

        // public void setItem(ItemStack stack)
        public void setItemStack(Supplier<ItemStack> stack) {
//            if (stack == null || stack.isEmpty()) return;
//            item = stack;
            CreativeTabManager.tabIconMap.put(this.name, stack);
        }

//        @Override
//        public ItemStack getTabIconItem() {
//            return item;
//        }

        public ResourceLocation getId() {
            return ResourceLocation.tryBuild(BCModules.BUILDCRAFT, this.name);
        }

        @Override
        public ResourceLocation getBackgroundLocation() {
            return new ResourceLocation("textures/gui/container/creative_inventory/tab_" + "items.png");
        }
    }
}
