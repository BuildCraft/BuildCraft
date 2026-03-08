/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
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
                        .info("[lib.migrate] Adding item migration from " + oldLowerCase + " to " + to.getRegistryName());
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
                        .info("[lib.migrate] Adding item migration from " + oldLowerCase + " to " + to.getRegistryName());
            }
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

    private static <T extends IForgeRegistryEntry<T>> void onMissingMappings(RegistryEvent.MissingMappings<T> missing, Map<String, T> migrations) {
        ImmutableList<Mapping<T>> all = missing.getAllMappings();
        if (all.isEmpty()) {
            return;
        }
        if (DEBUG) {
            BCLog.logger.info("[lib.migrate] Received missing mappings event for " + missing.getGenericType() + " with "
                    + all.size() + " missing.");
        }
        for (Mapping<T> mapping : all) {
            ResourceLocation loc = mapping.key;
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
                    BCLog.logger.info("[lib.migrate]    -> " + to.getRegistryName());
                }
            }
        }
    }
}
