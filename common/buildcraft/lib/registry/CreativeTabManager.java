/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.registry;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Forge {@code CreativeTabs} → Fabric {@link ItemGroup}.
 *
 * In 1.20.1 creative tabs are registry entries built via {@link FabricItemGroup#builder()} and
 * registered into {@link Registries#ITEM_GROUP}. The mutable icon of the legacy {@code CreativeTabBC}
 * is preserved through a captured {@link ItemStack} field read lazily by the icon supplier.
 */
public class CreativeTabManager {
    private static final Map<String, CreativeTabBC> tabMap = new HashMap<>();

    public static ItemGroup getTab(String name) {
        if (name.startsWith("vanilla.")) {
            // TODO(R.Chen): vanilla tab placement is data-driven in 1.20.1 — items are added to
            // vanilla groups via ItemGroupEvents.modifyEntriesEvent, not by returning the group.
            // Returning the BuildCraft tab as a fallback until the registration pass is ported.
        }
        if (tabMap.containsKey(name)) {
            return tabMap.get(name).getGroup();
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

    public static void setItem(String name, ItemConvertible item) {
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

    public static class CreativeTabBC {
        private ItemStack icon = new ItemStack(Items.COMPARATOR); // Temp.
        private final ItemGroup group;

        private CreativeTabBC(String name) {
            // Register the group into Registries.ITEM_GROUP. The icon supplier reads the mutable
            // field so a later setItem(...) call is reflected without rebuilding the group.
            Identifier id = new Identifier("buildcraftlib", name.replace('.', '_'));
            this.group = Registry.register(Registries.ITEM_GROUP, id,
                FabricItemGroup.builder()
                    .icon(() -> icon)
                    .displayName(Text.translatable("itemGroup." + name))
                    .build());
        }

        public ItemGroup getGroup() {
            return group;
        }

        public void setItem(ItemConvertible item) {
            if (item != null) {
                this.icon = new ItemStack(item);
            }
        }

        public void setItem(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return;
            icon = stack;
        }
    }
}
