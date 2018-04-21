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

    public static BlockAutoWorkbenchItems autoWorkbenchItems;
    public static BlockMiningWell miningWell;
    public static BlockPump pump;
    public static BlockTube tube;
    public static BlockFloodGate floodGate;
    public static BlockTank tank;
    public static BlockChute chute;
    public static BlockDistiller distiller;
    public static BlockHeatExchange heatExchange;

    // public static BlockAutoWorkbenchFluids autoWorkbenchFluids;
    public static BlockWaterGel waterGel;

    public static void fmlPreInit() {
        autoWorkbenchItems = RegistrationHelper.addBlockAndItem(new BlockAutoWorkbenchItems(Material.ROCK, "block.autoworkbench.item"));
        miningWell = RegistrationHelper.addBlockAndItem(new BlockMiningWell(Material.IRON, "block.mining_well"));
        pump = RegistrationHelper.addBlockAndItem(new BlockPump(Material.IRON, "block.pump"));
        tube = RegistrationHelper.addBlock(new BlockTube(Material.IRON, "block.tube"));
        floodGate = RegistrationHelper.addBlockAndItem(new BlockFloodGate(Material.IRON, "block.flood_gate"));
        tank = RegistrationHelper.addBlockAndItem(new BlockTank(Material.IRON, "block.tank"));
        chute = RegistrationHelper.addBlockAndItem(new BlockChute(Material.IRON, "block.chute"));
        distiller = RegistrationHelper.addBlockAndItem(new BlockDistiller(Material.IRON, "block.distiller"));
        heatExchange = RegistrationHelper.addBlockAndItem(new BlockHeatExchange(Material.IRON, "block.heat_exchange"));
        waterGel = RegistrationHelper.addBlock(new BlockWaterGel(Material.CLAY, "block.water_gel"));

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
