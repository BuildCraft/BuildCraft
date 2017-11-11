/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import net.minecraft.block.material.Material;

import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.factory.block.BlockAutoWorkbenchItems;
import buildcraft.factory.block.BlockChute;
import buildcraft.factory.block.BlockDistiller;
import buildcraft.factory.block.BlockFloodGate;
import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.block.BlockMiningWell;
import buildcraft.factory.block.BlockPump;
import buildcraft.factory.block.BlockTank;
import buildcraft.factory.block.BlockTube;
import buildcraft.factory.block.BlockWaterGel;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.factory.tile.TileHeatExchangeEnd;
import buildcraft.factory.tile.TileHeatExchangeStart;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileTank;

public class BCFactoryBlocks {

    private static final RegistrationHelper HELPER = new RegistrationHelper();

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
    // public static BlockPlastic plastic;
    public static BlockWaterGel waterGel;

    public static void fmlPreInit() {
        autoWorkbenchItems = HELPER.addBlockAndItem(new BlockAutoWorkbenchItems(Material.ROCK, "block.autoworkbench.item"));
        miningWell = HELPER.addBlockAndItem(new BlockMiningWell(Material.IRON, "block.mining_well"));
        pump = HELPER.addBlockAndItem(new BlockPump(Material.IRON, "block.pump"));
        tube = HELPER.addBlock(new BlockTube(Material.IRON, "block.tube"));
        floodGate = HELPER.addBlockAndItem(new BlockFloodGate(Material.IRON, "block.flood_gate"));
        tank = HELPER.addBlockAndItem(new BlockTank(Material.IRON, "block.tank"));
        chute = HELPER.addBlockAndItem(new BlockChute(Material.IRON, "block.chute"));
        distiller = HELPER.addBlockAndItem(new BlockDistiller(Material.IRON, "block.distiller"));
        heatExchange = HELPER.addBlockAndItem(new BlockHeatExchange(Material.IRON, "block.heat_exchange"));
        waterGel = HELPER.addBlock(new BlockWaterGel(Material.CLAY, "block.water_gel"));

        HELPER.registerTile(TileAutoWorkbenchItems.class, "tile.autoworkbench.item");
        HELPER.registerTile(TileMiningWell.class, "tile.mining_well");
        HELPER.registerTile(TilePump.class, "tile.pump");
        HELPER.registerTile(TileFloodGate.class, "tile.flood_gate");
        HELPER.registerTile(TileTank.class, "tile.tank");
        HELPER.registerTile(TileChute.class, "tile.chute");
        HELPER.registerTile(TileDistiller_BC8.class, "tile.distiller");
        HELPER.registerTile(TileHeatExchangeStart.class, "tile.heat_exchange.start");
        HELPER.registerTile(TileHeatExchangeEnd.class, "tile.heat_exchange.end");
    }
}
