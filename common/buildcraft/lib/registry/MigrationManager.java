/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

public enum MigrationManager {
    INSTANCE;

    private final Map<String, Item> itemMigrations = new HashMap<>();
    private final Map<String, Block> blockMigrations = new HashMap<>();

    public void addItemMigration(Item to, String... oldNames) {
        // If we mistakenly try to migrate null then it must have been disabled.
        if (to == null) {
            return;
        }
        for (String old : oldNames) {
            String oldLowerCase = old.toLowerCase(Locale.ROOT);
            if (itemMigrations.containsKey(oldLowerCase)) {
                throw new IllegalArgumentException("Already registered item migration \"" + oldLowerCase + "\"!");
            }
            itemMigrations.put(oldLowerCase, to);
        }
    }

    public void addBlockMigration(Block to, String... oldNames) {
        // If we mistakenly try to migrate null then it must have been disabled.
        if (to == null) {
            return;
        }
        for (String old : oldNames) {
            String oldLowerCase = old.toLowerCase(Locale.ROOT);
            if (blockMigrations.containsKey(oldLowerCase)) {
                throw new IllegalArgumentException("Already registered block migration \"" + oldLowerCase + "\"!");
            }
            blockMigrations.put(oldLowerCase, to);
        }
    }

    @SubscribeEvent
    public void onMissingBlocks(RegistryEvent.MissingMappings<Block> missing) {
        onMissingMappings(missing, blockMigrations);
    }

    @SubscribeEvent
    public void onMissingItems(RegistryEvent.MissingMappings<Item> missing) {
        onMissingMappings(missing, itemMigrations);
    }

    private static <T extends IForgeRegistryEntry<T>> void onMissingMappings(MissingMappings<T> missing,
        Map<String, T> migrations) {
        for (MissingMappings.Mapping<T> mapping : missing.getAllMappings()) {
            ResourceLocation loc = mapping.key;
            String domain = loc.getResourceDomain();
            String path = loc.getResourcePath().toLowerCase(Locale.ROOT);
            // TECHNICALLY this can pick up non-bc mods, but generally only addons
            if (!domain.startsWith("buildcraft")) continue;
            T to = migrations.get(path);
            if (to != null) {
                mapping.remap(to);
            }
        }
    }
}
