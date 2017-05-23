/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import net.minecraft.block.material.Material;

import buildcraft.api.enums.EnumLaserTableType;

import buildcraft.lib.BCLib;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
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
        laser = BlockBCBase_Neptune.register(new BlockLaser(Material.ROCK, "block.laser"));
        assemblyTable = BlockBCBase_Neptune.register(new BlockLaserTable(EnumLaserTableType.ASSEMBLY_TABLE, Material.ROCK, "block.assembly_table"));
        if (BCLib.DEV) {
            advancedCraftingTable = BlockBCBase_Neptune.register(new BlockLaserTable(EnumLaserTableType.ADVANCED_CRAFTING_TABLE, Material.ROCK, "block.advanced_crafting_table"));
        }
        integrationTable = BlockBCBase_Neptune.register(new BlockLaserTable(EnumLaserTableType.INTEGRATION_TABLE, Material.ROCK, "block.integration_table"));
        if (BCLib.DEV) {
            chargingTable = BlockBCBase_Neptune.register(new BlockLaserTable(EnumLaserTableType.CHARGING_TABLE, Material.ROCK, "block.charging_table"));
            programmingTable = BlockBCBase_Neptune.register(new BlockLaserTable(EnumLaserTableType.PROGRAMMING_TABLE, Material.ROCK, "block.programming_table"));
        }
        TileBC_Neptune.registerTile(TileLaser.class, "tile.laser");
        TileBC_Neptune.registerTile(TileAssemblyTable.class, "tile.assembly_table");
        if (BCLib.DEV) {
            TileBC_Neptune.registerTile(TileAdvancedCraftingTable.class, "tile.advanced_crafting_table");
        }
        TileBC_Neptune.registerTile(TileIntegrationTable.class, "tile.integration_table");
        if (BCLib.DEV) {
            TileBC_Neptune.registerTile(TileChargingTable.class, "tile.charging_table");
            TileBC_Neptune.registerTile(TileProgrammingTable_Neptune.class, "tile.programming_table");
        }
    }
}
