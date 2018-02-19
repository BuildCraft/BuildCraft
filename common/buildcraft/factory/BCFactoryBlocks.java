/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import buildcraft.factory.block.*;
import buildcraft.factory.tile.*;
import buildcraft.lib.registry.RegistrationHelper;
import net.minecraft.block.material.Material;

public class BCFactoryBlocks {

    public static void fmlPreInit() {
        RegistrationHelper.addBlockAndItem(new BlockAutoWorkbenchItems(Material.ROCK, "block.autoworkbench.item"));
        RegistrationHelper.addBlockAndItem(new BlockMiningWell(Material.IRON, "block.mining_well"));
        RegistrationHelper.addBlockAndItem(new BlockPump(Material.IRON, "block.pump"));
        RegistrationHelper.addBlock(new BlockTube(Material.IRON, "block.tube"));
        RegistrationHelper.addBlockAndItem(new BlockFloodGate(Material.IRON, "block.flood_gate"));
        RegistrationHelper.addBlockAndItem(new BlockTank(Material.IRON, "block.tank"));
        RegistrationHelper.addBlockAndItem(new BlockChute(Material.IRON, "block.chute"));
        RegistrationHelper.addBlockAndItem(new BlockDistiller(Material.IRON, "block.distiller"));
        RegistrationHelper.addBlockAndItem(new BlockHeatExchange(Material.IRON, "block.heat_exchange"));
        RegistrationHelper.addBlock(new BlockWaterGel(Material.CLAY, "block.water_gel"));

        RegistrationHelper.registerTile(TileAutoWorkbenchItems.class, "tile.autoworkbench.item");
        RegistrationHelper.registerTile(TileMiningWell.class, "tile.mining_well");
        RegistrationHelper.registerTile(TilePump.class, "tile.pump");
        RegistrationHelper.registerTile(TileFloodGate.class, "tile.flood_gate");
        RegistrationHelper.registerTile(TileTank.class, "tile.tank");
        RegistrationHelper.registerTile(TileChute.class, "tile.chute");
        RegistrationHelper.registerTile(TileDistiller_BC8.class, "tile.distiller");
        RegistrationHelper.registerTile(TileHeatExchange.class, "tile.heat_exchange");
    }
}
