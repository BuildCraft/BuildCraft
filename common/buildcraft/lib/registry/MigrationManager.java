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

import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.Type;

import buildcraft.api.core.BCLog;

public enum MigrationManager {
    INSTANCE;

    private final Map<String, Item> itemMigrations = new HashMap<>();
    private final Map<String, Block> blockMigrations = new HashMap<>();

    public void addItemMigration(Item to, String... oldNames) {
        // If we mistakenly try to migrate null then it must have been disabled.
        if (to == null || ForgeRegistries.ITEMS.getKey(to) == null) return;
        for (String old : oldNames) {
            String oldLowerCase = old.toLowerCase(Locale.ROOT);
            if (itemMigrations.containsKey(oldLowerCase)) throw new IllegalArgumentException("Already registered item migration \"" + oldLowerCase + "\"!");
            itemMigrations.put(oldLowerCase, to);
            BCLog.logger.info("Adding migration (ITEM) from " + old + " to " + to.getRegistryName());
        }
    }

    public void addBlockMigration(Block to, String... oldNames) {
        // If we mistakenly try to migrate null then it must have been disabled.
        if (to == null) return;
        for (String old : oldNames) {
            String oldLowerCase = old.toLowerCase(Locale.ROOT);
            if (blockMigrations.containsKey(oldLowerCase)) throw new IllegalArgumentException("Already registered block migration \"" + oldLowerCase + "\"!");
            blockMigrations.put(oldLowerCase, to);
            BCLog.logger.info("Adding migration (BLOCK) from " + old + " to " + to.getRegistryName());
        }
    }

    public void missingMappingEvent(FMLMissingMappingsEvent missing) {
        BCLog.logger.info("EVENT");
        for (MissingMapping mapping : missing.getAll()) {
            ResourceLocation loc = mapping.resourceLocation;
            String domain = loc.getResourceDomain();
            String path = loc.getResourcePath().toLowerCase(Locale.ROOT);
            // TECHNICALLY this can pick up non-bc mods, but generally only addons
            BCLog.logger.info("missing mapping " + loc);
            if (!domain.startsWith("buildcraft")) continue;
            BCLog.logger.info("  - of type buildcraft (" + mapping.type + ")");
            if (mapping.type == Type.ITEM) {
                if (itemMigrations.containsKey(path)) {
                    Item to = itemMigrations.get(path);
                    mapping.remap(to);
                    BCLog.logger.info("  - remapped to " + to.getRegistryName());
                    continue;
                }
            } else if (mapping.type == Type.BLOCK) {
                if (blockMigrations.containsKey(path)) {
                    Block to = blockMigrations.get(path);
                    mapping.remap(to);
                    BCLog.logger.info("  - remapped to " + to.getRegistryName());
                    continue;
                }
            }
            BCLog.logger.info("  - unknown");
        }
    }
}
