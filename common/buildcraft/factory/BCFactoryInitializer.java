/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.factory;

import net.fabricmc.api.ModInitializer;

import buildcraft.BuildCraftFabric;
import buildcraft.lib.BCLibInitializer;

/**
 * Fabric ModInitializer for the BuildCraft factory module.
 * Replaces the Forge {@code @Mod}-annotated {@link BCFactory} entry point.
 *
 * Lifecycle mapping (Forge {@link BCFactory} → Fabric):
 *   FMLPreInitializationEvent  → {@link #onInitialize()} (config, registry, proxy)
 *   FMLInitializationEvent     → {@link #onInitialize()} (folded; no-op at this stage)
 *   FMLPostInitializationEvent → {@link #onInitialize()} (folded; no-op at this stage)
 *   NetworkRegistry GUI handler → Fabric {@code ExtendedScreenHandlerType} (deferred)
 *   MinecraftForge.EVENT_BUS    → individual Fabric {@code Event} callbacks (deferred)
 *
 * NOTE(R.Chen): All block/item/tile/recipe registration is stubbed until lib.registry
 * (RegistrationHelper, TagManager) and lib.block/lib.item base classes are ported.
 */
public class BCFactoryInitializer implements ModInitializer {

    public static final String MODID = "buildcraftfactory";

    @Override
    public void onInitialize() {
        BuildCraftFabric.LOGGER.info("[BCFactory] Initializing buildcraft-factory (Fabric 1.20.1)");

        BuildCraftFabric.LOGGER.info(
            "[BCFactory] Depends on {} (lib entrypoint already built)", BCLibInitializer.MODID
        );

        // --- Forge preInit (BCFactory.preInit) -----------------------------------
        // STUB(R.Chen): RegistryConfig.useOtherModConfigFor(MODID, BCCoreInitializer.MODID) —
        //               port to Fabric config directory once BCCoreConfig is migrated.
        // STUB(R.Chen): BCFactoryBlocks.fmlPreInit() — migrate all blocks + tile entity types to
        //               Registry.register(Registries.BLOCK / Registries.BLOCK_ENTITY_TYPE, ...)
        //               once lib.block/lib.registry land.
        // STUB(R.Chen): BCFactoryItems.fmlPreInit() — migrate items to
        //               Registry.register(Registries.ITEM, ...) once lib.item lands.
        // STUB(R.Chen): NetworkRegistry GUI handler → ExtendedScreenHandlerType for each of:
        //               AUTO_WORKBENCH_ITEMS, CHUTE, TANK, DISTILLER.
        // STUB(R.Chen): MinecraftForge.EVENT_BUS.register(BCFactoryEventDist.INSTANCE) —
        //               port TextureStitchEvent to Fabric ClientSpriteRegistryCallback once
        //               factory render layer is migrated.
        // STUB(R.Chen): BCFactoryProxy.fmlPreInit() (client side) — port model/render init
        //               to BCFactoryClientInitializer.

        // --- Forge init (BCFactory.init) ----------------------------------------
        // STUB(R.Chen): BCFactoryProxy.fmlInit() — no factory-specific init hook at this stage.

        // --- Forge postInit (BCFactory.postInit) --------------------------------
        // STUB(R.Chen): BCFactoryProxy.fmlPostInit() — no factory-specific post-init hook.

        BuildCraftFabric.LOGGER.info("[BCFactory] Initialization complete (all registration stubbed)");
    }
}
