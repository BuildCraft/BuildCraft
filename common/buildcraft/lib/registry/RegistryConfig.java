/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import buildcraft.api.core.BCDebugging;

/**
 * Per-object enable/disable registry config.
 *
 * STUB(R.Chen): the Forge {@code Configuration}/{@code Property}/{@code Loader} system has no direct
 * Fabric equivalent. {@link #isEnabled} currently defaults everything to enabled; the {@code disabled}
 * set + {@link #setDisabled} bookkeeping is retained so a future Fabric config (e.g. a properties /
 * JSON reader, mirroring lib.config.FileConfigManager) can wire real values back in.
 *
 * STUB(R.Chen): the {@code IItemPipe} "pipes" category check is dropped here to avoid pulling the
 * unmigrated api.transport.pipe chain into this layer; restore once transport is migrated.
 */
public class RegistryConfig {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.registry");
    private static final Map<String, Set<String>> disabled = new HashMap<>();

    /** STUB(R.Chen): Forge per-mod config sharing — Phase 10. No-op for now. */
    public static void useOtherModConfigFor(String modId, String otherModId) {
        // STUB(R.Chen): config sharing deferred — Phase 10
    }

    // #######################
    //
    // Checking
    //
    // #######################

    public static boolean isEnabled(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return isEnabled(getCategory(item), id.getPath(), item.getTranslationKey() + ".name");
    }

    public static boolean isEnabled(Block block) {
        Identifier id = Registries.BLOCK.getId(block);
        return isEnabled(getCategory(block), id.getPath(), block.getTranslationKey() + ".name");
    }

    public static boolean isEnabled(String category, String resourcePath, String langKey) {
        // STUB(R.Chen): no config backing yet — everything is enabled by default.
        return true;
    }

    public static boolean hasItemBeenDisabled(Identifier loc) {
        return hasObjectBeenDisabled("items", loc) || hasObjectBeenDisabled("pipes", loc);
    }

    public static boolean hasBlockBeenDisabled(Identifier loc) {
        return hasObjectBeenDisabled("blocks", loc);
    }

    /** @return True if the given location has been passed to {@link #isEnabled(Block)}, {@link #isEnabled(Item)}, or
     *         {@link #isEnabled(String, String, String)}, and it returned false (because it has been disabled in the
     *         appropriate mod's config) */
    public static boolean hasObjectBeenDisabled(String category, Identifier loc) {
        Set<String> locations = disabled.get(category);
        return locations != null && locations.contains(loc.getPath());
    }

    // #######################
    //
    // Internals
    //
    // #######################

    private static String getCategory(Object obj) {
        // STUB(R.Chen): IItemPipe "pipes" category check removed (api.transport not migrated).
        if (obj instanceof Block) {
            return "blocks";
        } else {
            return "items";
        }
    }

    static void setDisabled(String category, String resourcePath) {
        disabled.computeIfAbsent(category, k -> new HashSet<>()).add(resourcePath);
    }
}
