/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import net.minecraft.block.material.Material;

import buildcraft.api.enums.EnumLaserTableType;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockLaserTable;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileChargingTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileLaser;
import buildcraft.silicon.tile.TileProgrammingTable_Neptune;

public class BCSiliconBlocks {

    public static BlockLaser laser;
    public static BlockLaserTable assemblyTable;
    public static BlockLaserTable advancedCraftingTable;
    public static BlockLaserTable integrationTable;
    public static BlockLaserTable chargingTable;
    public static BlockLaserTable programmingTable;

    public static void preInit() {
        laser = RegistrationHelper.addBlockAndItem(new BlockLaser(Material.IRON, "block.laser"));
        assemblyTable = createLaserTable(EnumLaserTableType.ASSEMBLY_TABLE, "block.assembly_table");
        advancedCraftingTable = createLaserTable(EnumLaserTableType.ADVANCED_CRAFTING_TABLE, "block.advanced_crafting_table");
        integrationTable = createLaserTable(EnumLaserTableType.INTEGRATION_TABLE, "block.integration_table");
        if (BCLib.DEV) {
            chargingTable = createLaserTable(EnumLaserTableType.CHARGING_TABLE, "block.charging_table");
            programmingTable = createLaserTable(EnumLaserTableType.PROGRAMMING_TABLE, "block.programming_table");
        }

        RegistrationHelper.registerTile(TileLaser.class, "tile.laser");
        RegistrationHelper.registerTile(TileAssemblyTable.class, "tile.assembly_table");
        RegistrationHelper.registerTile(TileAdvancedCraftingTable.class, "tile.advanced_crafting_table");
        RegistrationHelper.registerTile(TileIntegrationTable.class, "tile.integration_table");
        if (BCLib.DEV) {
            RegistrationHelper.registerTile(TileChargingTable.class, "tile.charging_table");
            RegistrationHelper.registerTile(TileProgrammingTable_Neptune.class, "tile.programming_table");
        }
    }

    private static BlockLaserTable createLaserTable(EnumLaserTableType type, String id) {
        BlockLaserTable block = new BlockLaserTable(type, Material.IRON, id);
        return RegistrationHelper.addBlockAndItem(block);
    }
}
