/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

import buildcraft.BuildCraftFabric;

/**
 * Fabric ModInitializer for the BuildCraft lib module.
 * Replaces the Forge {@code @Mod}-annotated {@link BCLib} entry point.
 *
 * Lifecycle mapping (Forge → Fabric):
 *   FMLPreInitializationEvent  → {@link #onInitialize()} (common-side init)
 *   FMLInitializationEvent     → {@link #onInitialize()} (folded; Fabric has no equivalent split)
 *   FMLPostInitializationEvent → {@link #onInitialize()} (folded; defer via Loom hooks if needed)
 *   FMLServerStartingEvent     → {@link ServerLifecycleEvents#SERVER_STARTING}
 *   ServerTickEvent (END)      → {@link ServerTickEvents#END_SERVER_TICK}
 *   WorldEvent.Unload          → {@link ServerWorldEvents#UNLOAD}
 */
public class BCLibInitializer implements ModInitializer {

    public static final String MODID = "buildcraftlib";

    @Override
    public void onInitialize() {
        BuildCraftFabric.LOGGER.info("[BCLib] Initializing buildcraft-lib (Fabric 1.20.1)");

        // STUB(R.Chen): registry hookups — wire up once BCLibBlocks/BCLibItems are migrated.
        // Registry.register(Registries.BLOCK, ...);
        // Registry.register(Registries.ITEM, ...);
        // Registry.register(Registries.BLOCK_ENTITY_TYPE, ...);

        // STUB(R.Chen): MessageManager.fmlPostInit() — port to Fabric Networking API (ServerPlayNetworking).
        // STUB(R.Chen): BuildCraftObjectCaches.fmlPreInit/fmlPostInit — port to Fabric lifecycle hooks.

        registerLifecycleEvents();

        BuildCraftFabric.LOGGER.info("[BCLib] Initialization complete");
    }

    private static void registerLifecycleEvents() {
        // Forge: FMLServerStartingEvent → register command
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            // STUB(R.Chen): port CommandBuildCraft to Fabric's CommandRegistrationCallback.
        });

        // Forge: ServerTickEvent (Phase.END)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // STUB(R.Chen): BCAdvDebugging.INSTANCE.onServerPostTick(); MessageUtil.postServerTick();
        });

        // Forge: WorldEvent.Unload (server side)
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            // STUB(R.Chen): MarkerCache.onWorldUnload(world); FakePlayerProvider.unloadWorld(world);
        });
    }
}
