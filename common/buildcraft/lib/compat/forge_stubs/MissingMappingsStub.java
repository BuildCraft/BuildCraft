/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.compat.forge_stubs;

import net.minecraft.util.Identifier;

import buildcraft.BuildCraftFabric;

/**
 * STUB(R.Chen): needs full implementation.
 *
 * Forge {@code RegistryEvent.MissingMappings} has no direct Fabric equivalent.
 * Fabric handles missing IDs by silently dropping unknown entries from saved worlds;
 * to remap legacy IDs we need either:
 *   - a custom {@code DataFixer} attached to the world save schema, or
 *   - a {@link net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents}
 *     hook that scans loaded chunks/items and rewrites known-renamed IDs.
 *
 * For now this class is a placeholder so call sites compile. The original
 * {@code MigrationManager} subscribed to {@code RegistryEvent.MissingMappings<Block>}
 * and {@code <Item>} and walked the {@code Mapping} list, calling {@code remap(...)}.
 */
public final class MissingMappingsStub {

    private MissingMappingsStub() {}

    /**
     * STUB(R.Chen): no-op. Fabric DataFixer integration is the proper replacement —
     * see {@link net.minecraft.datafixer.Schemas} and {@code QuiltDataFixes} (third-party).
     *
     * @param oldId legacy registry ID that may appear in old saves
     * @param newId current registry ID to remap onto
     */
    public static void registerRemap(Identifier oldId, Identifier newId) {
        BuildCraftFabric.LOGGER.debug("[stub] MissingMappingsStub.registerRemap({} -> {}) ignored — DataFixer not yet wired", oldId, newId);
    }
}
