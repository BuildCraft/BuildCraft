/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon;

import net.fabricmc.api.ModInitializer;

import buildcraft.BuildCraftFabric;
import buildcraft.lib.BCLibInitializer;

/**
 * Fabric ModInitializer for the BuildCraft silicon module.
 * Replaces the Forge {@code @Mod}-annotated {@link BCSilicon} entry point.
 *
 * Lifecycle mapping (Forge BCSilicon → Fabric):
 *   FMLPreInitializationEvent  → {@link #onInitialize()} (config, registry, proxy)
 *   FMLInitializationEvent     → {@link #onInitialize()} (folded; no-op at this stage)
 *   FMLPostInitializationEvent → {@link #onInitialize()} (folded; no-op at this stage)
 *   NetworkRegistry GUI handler → Fabric ExtendedScreenHandlerType (deferred)
 *   MinecraftForge.EVENT_BUS    → individual Fabric Event callbacks (deferred)
 *
 * NOTE(R.Chen): All block/item/tile/plug/statement registration is stubbed until
 * lib.registry (RegistrationHelper, TagManager) and relevant API layers are ported.
 */
public class BCSiliconInitializer implements ModInitializer {

    public static final String MODID = "buildcraftsilicon";

    @Override
    public void onInitialize() {
        BuildCraftFabric.LOGGER.info("[BCSilicon] Initializing buildcraft-silicon (Fabric 1.20.1)");

        BuildCraftFabric.LOGGER.info(
            "[BCSilicon] Depends on {} (lib entrypoint already built)", BCLibInitializer.MODID
        );

        // --- Forge preInit (BCSilicon.preInit) -----------------------------------
        // STUB(R.Chen): BCSiliconConfig.preInit() — port to Fabric config (FabricLoader.getConfigDir).
        // STUB(R.Chen): BCSiliconBlocks.preInit() — migrate all blocks + BlockEntityType registrations
        //               to Registry.register(Registries.BLOCK / BLOCK_ENTITY_TYPE, ...) once
        //               lib.block / lib.registry land.
        // STUB(R.Chen): BCSiliconItems.preInit() — migrate items to Registries.ITEM once lib.item lands.
        // STUB(R.Chen): BCSiliconPlugs.preInit() — register gate/lens/pulsar/lightSensor/timer/facade
        //               pluggables via PipeApi.pluggableRegistry once transport Phase 4E lands.
        // STUB(R.Chen): BCSiliconSprites.fmlPreInit() — register sprites via Fabric Atlas event.
        // STUB(R.Chen): NetworkRegistry GUI handler → ExtendedScreenHandlerType for each of:
        //               ASSEMBLY_TABLE, ADVANCED_CRAFTING_TABLE, INTEGRATION_TABLE, GATE.
        // STUB(R.Chen): MinecraftForge.EVENT_BUS.register(BCSiliconProxy) → Fabric lifecycle callbacks.

        // --- Forge init (BCSilicon.init) -----------------------------------------
        // STUB(R.Chen): BCSiliconRecipes.init() — port assembly/facade/swap recipes to Fabric
        //               recipe JSON + RecipeType/RecipeSerializer.
        // STUB(R.Chen): FacadeAPI.registerCompat() — deferred until FacadeStateManager lands.

        // --- Forge postInit (BCSilicon.postInit) ---------------------------------
        // STUB(R.Chen): BCSiliconStatements.preInit() is a static init call — safe to call here
        //               once StatementManager is wired up in libLeaf.
        BCSiliconStatements.preInit();
    }
}
