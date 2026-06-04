/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.silicon;

import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockLaserTable;

/**
 * STUB(R.Chen): BCSiliconBlocks registration deferred until lib.registry (RegistrationHelper,
 * Registries.BLOCK / BLOCK_ENTITY_TYPE) is migrated. Block instances are null placeholders.
 */
public class BCSiliconBlocks {

    // STUB(R.Chen): Block references are null until preInit() is wired.
    public static BlockLaser laser;
    public static BlockLaserTable assemblyTable;
    public static BlockLaserTable advancedCraftingTable;
    public static BlockLaserTable integrationTable;
    public static BlockLaserTable chargingTable;
    public static BlockLaserTable programmingTable;

    public static void preInit() {
        // STUB(R.Chen): Registry.register(Registries.BLOCK, ...) calls deferred.
    }
}
