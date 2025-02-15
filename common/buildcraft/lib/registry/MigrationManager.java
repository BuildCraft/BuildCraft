/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.ItemUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.MissingMappingsEvent.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum MigrationManager {
    INSTANCE;

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.migrate");

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
            if (DEBUG) {
                BCLog.logger
                        .info("[lib.migrate] Adding item migration from " + oldLowerCase + " to " + ItemUtil.getRegistryName(to));
            }
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
            if (DEBUG) {
                BCLog.logger
                        .info("[lib.migrate] Adding item migration from " + oldLowerCase + " to " + BlockUtil.getRegistryName(to));
            }
        }
    }

//    @SubscribeEvent
//    public void onMissingBlocks(RegistryEvent.MissingMappings<Block> missing) {
//        onMissingMappings(missing, blockMigrations);
//    }

//    @SubscribeEvent
//    public void onMissingItems(RegistryEvent.MissingMappings<Item> missing) {
//        onMissingMappings(missing, itemMigrations);
//    }

    @SubscribeEvent
    public void onMissingItemsAndBlocks(MissingMappingsEvent missing) {
        if (missing.getKey() == ForgeRegistries.BLOCKS.getRegistryKey()) {
            onMissingMappings(missing, ForgeRegistries.BLOCKS.getRegistryKey(), blockMigrations);
        } else if (missing.getKey() == ForgeRegistries.ITEMS.getRegistryKey()) {
            onMissingMappings(missing, ForgeRegistries.ITEMS.getRegistryKey(), itemMigrations);
        }
    }

    //    private static <T extends IForgeRegistryEntry<T>> void onMissingMappings(MissingMappingsEvent missing, Map<String, T> migrations)
    private static <T> void onMissingMappings(MissingMappingsEvent missing, ResourceKey<? extends Registry<T>> registryKey, Map<String, T> migrations) {
        List<MissingMappingsEvent.Mapping<T>> all = missing.getAllMappings(registryKey);
        if (all.isEmpty()) {
            return;
        }
        if (DEBUG) {
//            BCLog.logger.info("[lib.migrate] Received missing mappings event for " + missing.getGenericType() + " with "
            BCLog.logger.info("[lib.migrate] Received missing mappings event for " + missing.getKey().location() + " with "
                    + all.size() + " missing.");
        }
        for (Mapping<T> mapping : all) {
            ResourceLocation loc = mapping.getKey();
            String domain = loc.getNamespace();
            String path = loc.getPath().toLowerCase(Locale.ROOT);
            if (DEBUG) {
                BCLog.logger.info("[lib.migrate]  - " + domain + ":" + path);
            }
            // TECHNICALLY this can pick up non-bc mods, but generally only addons
            if (!domain.startsWith("buildcraft")) continue;
            T to = migrations.get(path);
            if (to != null) {
                mapping.remap(to);
                if (DEBUG) {
                    ResourceLocation registryName = null;
                    if (to instanceof Item toItem) {
                        registryName = ItemUtil.getRegistryName(toItem);
                    } else if (to instanceof Block toBlock) {
                        registryName = BlockUtil.getRegistryName(toBlock);
                    }
                    BCLog.logger.info("[lib.migrate]    -> " + registryName);
                }
            }
        }
    }
}
