/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.registry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

/**
 * Tracks old → new registry-name remappings for BuildCraft blocks and items.
 *
 * STUB(R.Chen): Forge's {@code RegistryEvent.MissingMappings} has no direct Fabric equivalent —
 * the {@code onMissingBlocks}/{@code onMissingItems} handlers are dropped. The remap data maps are
 * retained so a future Fabric-side fixup (e.g. a {@code DataFixer} or world-load scan) can consume
 * them. See {@code buildcraft/lib/compat/forge_stubs/MissingMappingsStub.java}.
 */
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
                    .info("[lib.migrate] Adding item migration from " + oldLowerCase + " to " + Registries.ITEM.getId(to));
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
                    .info("[lib.migrate] Adding block migration from " + oldLowerCase + " to " + Registries.BLOCK.getId(to));
            }
        }
    }

    // STUB(R.Chen): Forge RegistryEvent.MissingMappings<Block>/<Item> handlers removed.
    //               Re-implement remapping on the Fabric side once a world-load fixup exists;
    //               the itemMigrations / blockMigrations maps above carry the data.
}
