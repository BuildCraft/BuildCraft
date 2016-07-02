/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import buildcraft.factory.block.*;
import buildcraft.factory.block.BlockMiningWell;
import buildcraft.factory.block.BlockPlainPipe;
import buildcraft.factory.block.BlockPump;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.material.Material;

public class BCFactoryBlocks {
    public static BlockAutoWorkbenchItems autoWorkbenchItems;
    public static BlockMiningWell miningWell;
    public static BlockPump pump;
    public static BlockPlainPipe plainPipe;
    public static BlockTube tube;
    // public static BlockAutoWorkbenchFluids autoWorkbenchFluids;
    // public static BlockPlastic plastic;

    public static void preInit() {
        // plastic = BlockBuildCraftBase_BC8.register(new BlockPlastic("block.plastic"), ItemPlastic::new);
        autoWorkbenchItems = BlockBCBase_Neptune.register(new BlockAutoWorkbenchItems(Material.ROCK, "block.autoworkbench.item"));
        miningWell = BlockBCBase_Neptune.register(new BlockMiningWell(Material.ROCK, "block.mining_well"));
        pump = BlockBCBase_Neptune.register(new BlockPump(Material.ROCK, "block.pump"));
        plainPipe = BlockBCBase_Neptune.register(new BlockPlainPipe(Material.ROCK, "block.plain_pipe"));
        tube = BlockBCBase_Neptune.register(new BlockTube(Material.ROCK, "block.tube"));

        TileBC_Neptune.registerTile(TileAutoWorkbenchItems.class, "tile.autoworkbench.item");
        TileBC_Neptune.registerTile(TileMiningWell.class, "tile.mining_well");
        TileBC_Neptune.registerTile(TilePump.class, "tile.pump");
    }
}
